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

// For getting timestamp for filename:
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Timestamp;

public class Create_Finetunes {
  // Constant Filenames
  private static final String SCRAPED_STOCKS = "../data-scraper/scraped_stocks.txt";
  private static final String SCRAPED_NEWS = "../data-scraper/scraped_news.txt";
  private static final String FINE_TUNING_FILE_BASE = "./fine-tuning/fine-tuning-data_";
  private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'H'HH");

  // Constant Word Lists
  private static final String[] JUNK_WORDS = {"The", "Inc", "Incorporated", "Corp", "Corporation", "Company"};
  private static final String[] SIGNIFIER_WORDS = {"closed down", "stock", "share", "invest"};

  // Constant Prompt Templates
  private static final String[] PROMPT_TEMPLATES = {}; //TODO

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
    String company_name;
    String prompt, completion, finetune_line;
    while (stocks_in.hasNextLine()) {
      // Get next company name and generate prompt
      company_name = GetNextCompanyName(stocks_in);
      prompt = CreatePrompt(company_name);

      // Get stock movement data and begin completion
      completion = GetStockMovement(stocks_in);

      // Search for company name in scraped_news and pull out relevant sentence if found
      String relevant_news = "";
        // find sentence...

      completion += relevant_news;

      // Do final formatting on completion?

      // Get formatted line for fine-tuning file using prompt and completion, and then write to file
      finetune_line = GetJSONLLine(prompt, completion);
      try { ft_out.write(finetune_line); }
      catch (Exception e) { System.out.println("Could not write finetune line -- " +e); }

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
     args: stocks_in | Scanner object currently in scraped_stocks.txt
     : This method identifies the next company name in scraped_stocks.txt and returns just the name
       of the company, with all junk words removed.
  */
  private static String GetNextCompanyName(Scanner stocks_in) {
    String company_name = "";

    return company_name;
  }


  /*
    CleanJunkWords(String str)
     args: str | String being cleaned
     : This method removes any instances of a word from JUNK_WORDS from str and returns the result.
  */
  private static String CleanJunkWords(String str) {
    String cleaned_str = "";

    return cleaned_str;
  }


  /*____________________________________________________________________________________________________
    CreatePrompt(String company_name)
     args: company_name | Name of company about whom prompt is being created
     : This methods randomly creates a prompt from a list of templates (PROMPT_TEMPLATES) and returns
       the prompt.
  */
  private static String CreatePrompt(String company_name) {
    String prompt = "";
    // don't forget '->'
    return prompt;
  }


  /*____________________________________________________________________________________________________
    GetStockMovement(Scanner stock_in)
     args stocks_in | Scanner object currently in scraped_stocks.txt
     : This method gets the line containing the stock movement data and pulls out the change in percentage
       of the stock price for the day. It returns the change as a sentence formatted to be a completion.
  */
  private static String GetStockMovement(Scanner stocks_in) {
    String stock_movement = "";
    // don't forget to add a ' ' to beginning of output
    return stock_movement;
  }


  private static boolean IdentifySignifierWords(String str) {
    return false;
  }


  private static String GetJSONLLine(String prompt, String completion) {
    String finetune_line = "";

    return finetune_line;
  }
}