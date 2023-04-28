/*
    Create_Finetunes.java
      Author: Charlotte Valentine

      This program processes the data produced by the web scraping component and converts
      the data into prompt-completion pairs of the correct format for fine-tuning.
*/
import java.io.File;                    // Interfacing with data scraper files
import java.util.Scanner;               // Reading from files
import java.util.StringTokenizer;       // Breaking up data scraping input
import java.io.FileWriter;              // Logging output/formatted fine-tuning data

// For getting timestamp for fine-tuning filename:
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Timestamp;

import java.util.Random;                // Random class to randomly choose prompt templates

// For getting timestamp for fine-tuning prompt:
import java.time.LocalDate;


public class Create_Finetunes {
  // Constant Filenames (relative to gpt-api directory, probably bad form)
  private static final String SCRAPED_STOCKS = "../data-scraper/scraped_stocks.txt";
  private static final String SCRAPED_NEWS = "../data-scraper/scraped_news.txt";
  private static final String FINE_TUNING_FILE_BASE = "./fine-tuning/fine-tuning-data_";
  private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'H'HH");

  // Constant Word Lists
  private static final String[] JUNK_WORDS = {"The", "Inc", "Incorporated", "Corp", "Corporation", "Company", "Co", "Holdings"};
  private static final String[] SIGNIFIER_WORDS = {"earning", "investor", "analyst", "estimate", "sector", "stock", "shares", "investment", "revenue", "-quarter"};

  // Constant Prompt Templates
  //   Key: # (Date), * (Company Name), $ (behavior present-tense (go up, go down, etc)), % (behavior past-tense (went up, went down))
  private static final String[] PROMPT_TEMPLATES = {"Today is #. How did *'s stock price change today? ->",
                                                    "What happened to the share price of * today? It's #. ->",
                                                    "I noticed that *'s share price % today, on #. Why is that? ->",
                                                    "What's the story with *'s shares? Today is #. ->",
                                                    "Why did *'s stock price $ today? It's #. ->",
                                                    "By how much did *'s stock price $ on #? ->"};

  // Constant Completion Templates
  //   Key: * (Company Name), % (percentage change), @ (increased, decreased), # (up, down), & (jumped up, fell down), ^ (+, -)
  private static final String[] COMPLETION_TEMPLATES = {"*'s share price @ by % today.",                        // increased, decreased
                                                        "* closed the day at % #.",                             // up, down
                                                        "* stocks' & today, ending at % #.",                    // jumped up, fell down
                                                        "By the end of the day, *'s stock price was # by %.",   // up, down 
                                                        "* saw a change of ^% in terms of stock price today."}; // +, -      

  /*
    ProduceFinetuningData
     args:
     : This method combs through the scraped stocks and news produced by the data scraper. For each
       company reported in the stocks file, the method attempts to identify a corresponding piece of
       news from the news file to create a completion. It returns the filename pointing to the file
       containing the produced prompt/completion pairs.
  */
  public static String ProduceFinetuningData() {
    // Open Scraped Stocks
    File stocks;
    Scanner stocks_in;
    try {
      stocks = new File(SCRAPED_STOCKS);
      stocks_in = new Scanner(stocks);
    } catch(Exception e) {
      System.out.println(e);
      return null;
    }

    // Create Finetuning File
    String finetuning = GenerateFinetuningFilename();
    File ft_file;
    FileWriter ft_out;
    try {
      ft_file = new File(finetuning);
      ft_out = new FileWriter(ft_file);
    } catch (Exception e) {
      System.out.println(e);
      stocks_in.close();
      return null;
    }


    // Begin Combing through Stocks & News
    System.out.println("Combing through scraped data...");
    String prompt, completion, finetune_line;
    String previous_company_name;
    String company_name = GetNextCompanyName(stocks_in, "");
    String stock_movement;
    while (stocks_in.hasNextLine()) {
      stock_movement = stocks_in.nextLine();                                // You have to get the next line before creating the prompt; need to know if stock increases or decreases

      // Generate prompt using company name and flag (true if price increases or stays same, false if decreases)
      if (stock_movement.contains("+")) { prompt = CreatePrompt(company_name, true); }
      else { prompt = CreatePrompt(company_name, false); }

      // Begin generating completion using stock movement
      completion = GetStockMovement(company_name, stock_movement);

      // Search for company name in scraped_news and pull out relevant sentence if found
      String relevant_news = "";
      relevant_news = GetRelevantNews(company_name);
      completion += relevant_news;

      // Get formatted line for fine-tuning file using prompt and completion, and then write to file
      finetune_line = GetJSONLLine(prompt, completion)+ "\n";
      try { ft_out.write(finetune_line); }
      catch (Exception e) { System.out.println("  Could not write finetune line -- " +e); }

      // Get next company name (if exists)
      previous_company_name = company_name;
      company_name = GetNextCompanyName(stocks_in, previous_company_name);
    }


    // Cleanup
    stocks_in.close();
    try { ft_out.close(); }
    catch (Exception e) {
      System.out.println(e);
    }

    // Return Filename of Fine-Tuning Data
    return finetuning;
  }

  
  /*____________________________________________________________________________________________________
    GenerateFinetuningFilename()
     args:
     : This method generates the finetuning filename for today's finetuning. It gets the current timestamp
       appends it to FINE_TUNING_FILE_BASE, then returns the new filename.
  */
  private static String GenerateFinetuningFilename() {
    // Generate Timestamp for Filename
    Date currentDate = new Date();
    Timestamp ts = new Timestamp(currentDate.getTime());
    String timestamp = SDF.format(ts);                      // yyyy-mm-ddThh

    // Combine Filename Base, Timestamp, and File Extension
    String finetuning = FINE_TUNING_FILE_BASE;
    finetuning += timestamp;
    finetuning += ".jsonl";
    
    // Return Filename
    return finetuning;
  }


  /*____________________________________________________________________________________________________
    GetNextCompanyName(Scanner stocks_in, String previous_company_name)
     args:             stocks_in | Scanner object currently in SCRAPED_STOCKS
           previous_company_name | Previous company name to check for duplicates
     : This method identifies the next company name in SCRAPED_STOCKS and returns just the name
       of the company, with all junk words removed.
  */
  private static String GetNextCompanyName(Scanner stocks_in, String previous_company_name) {
    String current_line, cutoff_line = "";
    String company_name = "";

    // Find Line Containing next Company Name
    current_line = stocks_in.nextLine();
    while (!current_line.substring(0, 4).equals("http")) {         // Messy: go through document until you hit a line with a url (urls precede company names)
      if (!stocks_in.hasNextLine()) { return null; }                                            // If you reach the end of the document, return null
      
      current_line = stocks_in.nextLine();
      if (current_line.equals("")) { current_line = GetNextNonemptyLine(stocks_in); }  // If next line is empty, find next nonempty line
    }
    current_line = stocks_in.nextLine();                                                        // current_line will now be the line containing the company name

    // Isolate Company Name from Line
    for (int i = 0; i < current_line.length(); i++) {
      // If you reach a ',' cutoff the line
      if (current_line.charAt(i) == ',') {
        cutoff_line = current_line.substring(0, i);
        break;
      // If you reach a '(' cutoff the line
      } else if (current_line.charAt(i) == '(') {
        cutoff_line = current_line.substring(0, i);
        break;
      }
    }

    // Remove Junk Words
    company_name = CleanJunkWords(cutoff_line);
    // Sometimes in the data the same company is repeated twice; band-aid solution to skip over it
    if (company_name.equals(previous_company_name)) { company_name = GetNextCompanyName(stocks_in, ""); }

    // Return Company Name
    return company_name;
  }

  /*
    GetNextNonemptyLine(Scanner stocks_in)
     args: stocks_in | Scanner object currently in SCRAPED_STOCKS
     : This method finds the next line in SCRAPED_STOCKS that is not empty; i.e. not "\n". If it reaches
       the end of the file, it returns "junkdata". This is to avoid an outofbounds error in GetCompanyName().
  */
  private static String GetNextNonemptyLine(Scanner stocks_in) {
    String current_line = "";

    while (current_line.equals("")) {
      if (!stocks_in.hasNextLine()) { return "junkdata"; }
      current_line = stocks_in.nextLine();
    }

    return current_line;
  }

  /*
    CleanJunkWords(String str)
     args: str | String being cleaned
     : This method removes any instances of a word from JUNK_WORDS from str and returns the result.
  */
  private static String CleanJunkWords(String str) {
    StringTokenizer st = new StringTokenizer(str, " ");               // Split str into tokens based on " " delimiter
    String word, cleaned_str = "";
    boolean firstWord = true;
    
    // Loop through Tokens -- Add to cleaned_str if not Junk Word
    while (st.hasMoreTokens()) {
      word = st.nextToken(); 
      
      word = word.trim();
      for (int i = 0; i < JUNK_WORDS.length; i++) {
        if (word.equals(JUNK_WORDS[i])) {                                   // If word is a junk word, make empty
          word = "";
          break;
        }
      }

      // If first word, do not add leading " "
      if (!word.equals("") && firstWord) {
        firstWord = false;
      // If it's a subsequent word, add a leading " "
      } else if (!word.equals("")) {
        word = " " + word;
      }

      cleaned_str += word;
    }

    return cleaned_str;
  }


  /*____________________________________________________________________________________________________
    CreatePrompt(String company_name)
     args: company_name | Name of company about whom prompt is being created
     : This methods randomly creates a prompt from a list of templates (PROMPT_TEMPLATES) and returns
       the prompt.
  */
  private static String CreatePrompt(String company_name, boolean increase) {
    // Random used to randomly generate prompt from templates
    Random rnd = new Random();

    // List of descriptors of stock behavior, dependent on template
    final String[] behavior_past_tense = {"went up", "increased", "went down", "decreased"};
    final String[] behavior_present_tense = {"go up", "increase", "go down", "decrease"};

    // Get Date for Prompt
    String date = GetDayMonthYear();

    // Build Randomly Generated Prompt from Templates
    //   Key: # (Date), * (Company Name), $ (behavior present-tense (go up, go down, etc)), % (behavior past-tense (went up, went down))
    int template = rnd.nextInt(PROMPT_TEMPLATES.length);
    int descriptor;
    String prompt = PROMPT_TEMPLATES[template];
    
    prompt = prompt.replace("#", date);           // Insert date
    if (prompt.contains("$")) {                        // If prompt template has present-tense symbol, randomly choose present-tense descriptor
      if (increase) {
        descriptor = rnd.nextInt(0, 2);
      } else {
        descriptor = rnd.nextInt(2, 4);
      }
      prompt = prompt.replace("$", behavior_present_tense[descriptor]);
    } else if (prompt.contains("%")) {                 // If prompt template has past-tense symbol, randomly choose past-tense descriptor
      if (increase) {
        descriptor = rnd.nextInt(0, 2);
      } else {
        descriptor = rnd.nextInt(2, 4);
      }
      prompt = prompt.replace("%", behavior_past_tense[descriptor]);
    }
    prompt = prompt.replace("*", company_name);   // Insert company name

    return prompt;
  }

  private static String GetDayMonthYear() {
    String date = "";
    LocalDate currentDate = LocalDate.now();

    date += currentDate.getMonth().toString();
    date = date.toLowerCase();
    date = ((char) (date.charAt(0) - 32))+ "" +date.substring(1);                    // Capitalize first letter of month
    date += " " +currentDate.getDayOfMonth();
    date += ", " +currentDate.getYear();

    return date;
  }


  /*____________________________________________________________________________________________________
    GetStockMovement(String company_name, String stock_movement)
     args:   company_name | String containing name of the company whose stock price changed
           stock_movement | String containing line showing stock behavior from SCRAPED_STOCKS
     : This method gets the line containing the stock movement data and pulls out the change in percentage
       of the stock price for the day. It returns the change as a sentence formatted to be a completion.
  */
  private static String GetStockMovement(String company_name, String stock_movement) {
    // Single Out Percent Change in Stock
    String percent_change;
    if ((percent_change = GetPercentChange(stock_movement)) == null) { return ""; }

    // Record Sign of Percent for choosing Descriptors
    char sign = percent_change.charAt(0);
    int sign_index;
    if (sign == '+') { sign_index = 0; }
    else { sign_index = 1; }
    percent_change = percent_change.substring(1);        // Remove +/- sign from beginning of percent_change
    percent_change += "%";

    // List of descriptors of stock behavior
    String[] descriptor_type1 = {"increased", "decreased"};
    String[] descriptor_type2 = {"up", "down"};
    String[] descriptor_type3 = {"jumped up", "fell down"};
    String[] descriptor_type4 = {"+", "-"};

    // Create Completion from Template
    //   Key: * (Company Name), % (percentage change), @ (increased, decreased), # (up, down), & (jumped up, fell down), ^ (+, -)
    Random rnd = new Random();                                      // Random used to randomly generate completion from templates
    int template = rnd.nextInt(COMPLETION_TEMPLATES.length);
    String completion = " " +COMPLETION_TEMPLATES[template];

    completion = completion.replace("*", company_name);      // Insert company name
    completion = completion.replace("%", percent_change);    // Insert percent change
    completion = completion.replace("@", descriptor_type1[sign_index]);
    completion = completion.replace("#", descriptor_type2[sign_index]);
    completion = completion.replace("&", descriptor_type3[sign_index]);
    completion = completion.replace("^", descriptor_type4[sign_index]);

    return completion;
  }

  /*
    GetPercentChange(String stock_movement)
     args: stock_movement | Raw input string containing stock statistics
     : This method extracts the percent change from the raw input and returns it.
  */
  private static String GetPercentChange(String stock_movement) {
    // Tokenize stock_movement; final token will always be percent change
    StringTokenizer st = new StringTokenizer(stock_movement);
    if (!st.hasMoreTokens()) { return null; }

    String percentChange = "";
    while (st.hasMoreTokens()) {
      percentChange = st.nextToken();
    }
    percentChange = percentChange.trim();

    return percentChange;
  }


  /*____________________________________________________________________________________________________
    GetRelevantNews(String company_name)
     args: company_name | Name of the company for which you are trying to find news
     : This method goes through the SCRAPED_NEWS file and searches for news pertaining to the company
       given as an argument. It returns a line from SCRAPED_NEWS considered valid if it finds one, or
       returns the empty string if nothing could be found.
  */
  private static String GetRelevantNews(String company_name) {
    // Open SCRAPED_NEWS File
    File news;
    Scanner news_in;
    try {
      news = new File(SCRAPED_NEWS);
      news_in = new Scanner(news);
    } catch (Exception e) {
      System.out.println(e);
      return "";
    }
    
    if (!news_in.hasNextLine()) { 
      news_in.close();
      return "";
    }


    // Find Relevant News based on SIGNIFIER_WORDS in SCRAPED_NEWS
    String relevant_news = "";
    String current_line = news_in.nextLine();
    // Loop through entire file, one line at a time, until you find a valid piece of news (or not)
    while (news_in.hasNextLine()) {
      // Small speedup? Ignore URLs
      if (current_line.substring(0,4).equals("http")) { }
      // If current line contains the company name and has a SIGNIFIER_WORD, treat it as valid news and end the loop
      else if (current_line.contains(company_name) &&
          IdentifySignifierWords(current_line)) {
            relevant_news = " " +current_line;
            break;
      }

      current_line = news_in.nextLine();
      if (current_line.equals("")) { current_line = GetNextNonemptyLine(news_in); }  // If next line is empty, find next nonempty line
      if (current_line.length() < 4) { current_line = "junkdata"; }                           // If current line is smaller than 4 characters, treat as junk data
    }

    // Loop through Relevant News -- Remove Junk Characters
    for (int i = 0; i < relevant_news.length(); i++) {
      if (((int) relevant_news.charAt(i)) < 32) {
        relevant_news = relevant_news.substring(0, i) + relevant_news.substring(i+1);
        i--;
      }
    }

    // Cleanup
    try { news_in.close(); } 
    catch (Exception e) {
      System.out.println(e);
      return relevant_news;
    }

    // Return Relevant News
    return relevant_news;
  }

  /*
    IdentifySignifierWords(String current_line)
     args: current_line | String where you are looking for signifiers
     : This method looks for a SIGNIFIER_WORD in current_line. If it finds one, it returns true. Otherwise, it returns
       false.

       note: possible alternate implementation for consideration. Find every line containing a signifier word in the file,
             and record # of signifier words in each. The line with the highest word count is used.
  */
  private static boolean IdentifySignifierWords(String current_line) {
    // Tokenize Input String
    StringTokenizer st = new StringTokenizer(current_line);
    if (!st.hasMoreTokens()) { return false; }

    // Check for Signifier Words
    boolean is_signifier = false;
    String word;
    while(st.hasMoreTokens()) {
      word = st.nextToken();
      word = word.trim();

      // Check word against each SIGNIFIER_WORDS
      for (int i = 0; i < SIGNIFIER_WORDS.length; i++) {
        if (word.equals(SIGNIFIER_WORDS[i])) {
          is_signifier = true;
          break;
        }
      }

      // If you've found a signifier word, end the loop
      if (is_signifier) { break; }
    }

    // Return Result
    return is_signifier;
  }


  /*____________________________________________________________________________________________________
    GetJSONLine(String prompt, String completion)
     args:     prompt | Generated prompt
           completion | Generated completion
     : This method produces a formatted JSONL line and returns it.
  */
  private static String GetJSONLLine(String prompt, String completion) {
    if (prompt.equals("") || prompt == null) { return ""; }
    if (completion.equals("") || completion == null) { return ""; }

    // Fix completion formatting (replace " with \")
    completion = FormatCompletion(completion);

    // Create JSONL-formatted Fine-Tune
    // {"prompt":"+prompt+","completion":"+completion+\n"}
    String finetune_line;
    finetune_line = "{\"prompt\": ";
    finetune_line += "\"" +prompt+ "\", ";
    finetune_line += "\"completion\": ";
    finetune_line += "\"" +completion+ "\\n\"}";

    System.out.println(finetune_line);
    return finetune_line;
  }

  private static String FormatCompletion(String completion) {
    String formatted_completion = completion;

    for (int i = 0; i < formatted_completion.length(); i++) {
      if (formatted_completion.charAt(i) == '\"') {
        formatted_completion = formatted_completion.substring(0,i) +"\\\""+ formatted_completion.substring(i+1);
        i++;
      }
    }

    return formatted_completion;
  }
}


  // Go through scraped_stocks.txt --
  //   delimiter between company names is url; they all start with http so go through file line-by-line until you identify that sequence of characters
  //   read next line and tokenize
  //     go through tokens; the delimiter for the company name is the '(' used for the stock ticker name
  //     once you isolate the full company name, eliminate any 'junk' words

  //   search through scraped_news.txt for the first instance of the company name
  //   take the whole paragraph encompassing the company name and tokenize
  //     search for any instance of a 'signifier' word
  //     if you find one, return that paragraph as the part of the completion for the current company
  //     if not, go to next instance of company name and repeat
  //   if you get to end of document with no success, return empty string