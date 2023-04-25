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
import java.util.Date;
import java.time.Month;
import java.time.LocalDate;


public class Create_Finetunes {
  // Constant Filenames (relative to gpt-api directory, probably bad form)
  private static final String SCRAPED_STOCKS = "../data-scraper/scraped_stocks.txt";
  private static final String SCRAPED_NEWS = "../data-scraper/scraped_news.txt";
  private static final String FINE_TUNING_FILE_BASE = "./fine-tuning/fine-tuning-data_";
  private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'H'HH");

  // Constant Word Lists
  private static final String[] JUNK_WORDS = {"The", "Inc", "Incorporated", "Corp", "Corporation", "Company", "Co", "Holdings"};
  private static final String[] SIGNIFIER_WORDS = {"closed down", "stock", "share", "invest"};

  // Constant Prompt Templates
  //   Key: # (Date), * (Company Name), $ (behavior present-tense (go up, go down, etc)), % (behavior past-tense (went up, went down))
  private static final String[] PROMPT_TEMPLATES = {"Today is #. How did *'s stock price change today? ->",
                                                    "What happened to the share price of * today? It's #. ->",
                                                    "I noticed that *'s share price % today, on #. Why is that? ->",
                                                    "Should I invest in *? Today is #. ->",
                                                    "What's the story with *'s shares? Today is #. ->",
                                                    "Why did *'s stock price $ today? It's #. ->",
                                                    "By how much did *'s stock price $ on #? ->"};

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

  /*
    ProduceFinetuningData
     args:
     : This method combs through the scraped stocks and news produced by the data scraper. For each
       company reported in the stocks file, the method attempts to identify a corresponding piece of
       news from the news file to create a completion. It returns the filename pointing to the file
       containing the produced prompt/completion pairs.
  */
  public static String ProduceFinetuningData() {
    // Open Scraped Stocks & Scraped News
    File stocks, news;
    Scanner stocks_in, news_in;
    try {
      stocks = new File(SCRAPED_STOCKS);
      stocks_in = new Scanner(stocks);

      news = new File(SCRAPED_NEWS);
      news_in = new Scanner(news);
      
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
      news_in.close();
      return null;
    }


    // Begin Combing through Stocks & News
    System.out.println("Combing through scraped data...");
    String stock_movement;
    String prompt, completion, finetune_line;
    String company_name = GetNextCompanyName(stocks_in);
    while (stocks_in.hasNextLine()) {
      stock_movement = stocks_in.nextLine();                                // You have to get the next line before creating the prompt; need to know if stock increases or decreases

      // Generate prompt using company name and flag (true if price increases or stays same, false if decreases)
      if (stock_movement.contains("+")) { prompt = CreatePrompt(company_name, true); }
      else { prompt = CreatePrompt(company_name, false); }

      // Get stock movement data and begin completion
      completion = GetStockMovement(stock_movement);

      // Search for company name in scraped_news and pull out relevant sentence if found
      String relevant_news = "";
        // find sentence...

      completion += relevant_news;

      // Do final formatting on completion?

      // Get formatted line for fine-tuning file using prompt and completion, and then write to file
      finetune_line = GetJSONLLine(prompt, completion);
      try { ft_out.write(finetune_line); }
      catch (Exception e) { System.out.println("Could not write finetune line -- " +e); }

      // Get next company name (if exists)
      company_name = GetNextCompanyName(stocks_in);
    }


    // Cleanup
    stocks_in.close();
    news_in.close();
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
    GetNextCompanyName(Scanner stocks_in)
     args: stocks_in | Scanner object currently in SCRAPED_STOCKS
     : This method identifies the next company name in SCRAPED_STOCKS and returns just the name
       of the company, with all junk words removed.
  */
  private static String GetNextCompanyName(Scanner stocks_in) {
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
      if (firstWord) {
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
    prompt = prompt.replace("*", company_name);   // Insert company name
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

    System.out.println(prompt+ "\n");
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
    GetStockMovement(String stock_movement)
     args: stock_movement | String containing line showing stock behavior from SCRAPED_STOCKS
     : This method gets the line containing the stock movement data and pulls out the change in percentage
       of the stock price for the day. It returns the change as a sentence formatted to be a completion.
  */
  private static String GetStockMovement(String stock_movement) {
    String output = "";
    // don't forget to add a ' ' to beginning of output
    return output;
  }


  private static boolean IdentifySignifierWords(String str) {
    return false;
  }


  private static String GetJSONLLine(String prompt, String completion) {
    String finetune_line = "";

    return finetune_line;
  }
}