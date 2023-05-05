/*
    Train-Model.java
      Author: Charlotte Valentine

      This program takes the fine-tuning data produced by Create-Finetunes and actually trains the model
      with it.

      This was never completed; you have to manually train the model from the command line using API commands.
*/

// For Handling Openai API Commands
import java.lang.Process;
import java.lang.ProcessBuilder;
import java.lang.Runtime;

// For Handling Model Name (stored in file)
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;


public class Train_Model {
  private static String API_TRAIN_CMD = "openai api fine_tunes.create";
  private static String API_TRAIN_ARGS = "--n_epochs 1 --prompt_loss_weight .5";

  private static String MODEL_FILE = "current_model.txt";


  public static boolean TrainModel(String finetunes) {
    // Get Old Model Name
    String old_model;
    if ((old_model = GetOldModelName()) == null) {
      return false;
    }


    // Generate API Command
    String command = "";
    command += API_TRAIN_CMD;                           // Add command name 
    command += " -t ./fine-tuning/" +finetunes;         // Add fine-tuning data file as argument
    command += " -m " +old_model;                       // Add old model name as argument
    command += " " +API_TRAIN_ARGS;                     // Add other arguments


    // Run API Command
    String api_output = "";
    api_output = RunAPICommand(command);

    
    // Log New Model Name
    String new_model = GetNewModelName(api_output);
    if (!WriteModelName(new_model)) {                   // If writing model name fails, output name to terminal as last resort
      System.out.println("Failed to log new model name. Output name here instead:");
      System.out.println(new_model);
    }

    // Return Status
    return true;
  }


  /*
    GetOldModelName()
     args:
     : This method reads the first line in MODEL_FILE (the model name) and returns it.
  */
  private static String GetOldModelName() {
    // Open File & Scanner
    File model_file;
    Scanner model_in;
    try {
      model_file = new File(MODEL_FILE);
      model_in = new Scanner(model_file);
    } catch (Exception e) {
      System.out.println(e);
      return null;
    }

    if (!model_in.hasNextLine()) {
      model_in.close();
      return null;
    }

    // Get Model Name
    String old_model = model_in.nextLine();
    old_model = old_model.trim();

    // Cleanup
    model_in.close();

    // Return Model Name
    return old_model;
  }


  /*
  */
  private static String RunAPICommand(String command) {
    String output = "";

    return output;
  }


  /*
  */
  private static String GetNewModelName(String command_output) {
    String new_model = "";

    return new_model;
  }


  /*
  */
  private static boolean WriteModelName(String model_name) {
    // Open File & FileWriter
    File model_file;
    FileWriter file_out;
    try {
      model_file = new File(MODEL_FILE);
      file_out = new FileWriter(model_file);
    } catch (Exception e) {
      System.out.println(e);
      return false;
    }


    // Write New Model Name to File
    boolean is_successful;
    try {
      file_out.write(model_name);
      is_successful = true;
    } catch (Exception e) {
      System.out.println(e);
      is_successful = false;
    }


    // Cleanup
    try { 
      file_out.close();
    } catch (Exception e) {
      System.out.println(e);
    }

    return is_successful;
  }
}

/* Output format:
[2023-04-28 20:52:40] Created fine-tune: ft-xjNnX4LDLyhaf1jHiXV1RDGz
[2023-04-28 21:40:45] Fine-tune costs $0.63
[2023-04-28 21:40:46] Fine-tune enqueued. Queue number: 10
[2023-04-28 21:41:01] Fine-tune is in the queue. Queue number: 9
[2023-04-28 21:41:02] Fine-tune is in the queue. Queue number: 8
[2023-04-28 21:41:14] Fine-tune is in the queue. Queue number: 7
[2023-04-28 21:42:04] Fine-tune is in the queue. Queue number: 6
[2023-04-28 21:42:18] Fine-tune is in the queue. Queue number: 5
[2023-04-28 21:43:40] Fine-tune is in the queue. Queue number: 4
[2023-04-28 21:44:22] Fine-tune is in the queue. Queue number: 3
[2023-04-28 21:44:42] Fine-tune is in the queue. Queue number: 2
[2023-04-28 21:46:00] Fine-tune is in the queue. Queue number: 1
[2023-04-28 21:46:39] Fine-tune is in the queue. Queue number: 0
[2023-04-28 21:46:41] Fine-tune started
[2023-04-28 21:51:38] Completed epoch 1/1
[2023-04-28 21:52:18] Uploaded model: davinci:ft-csstockt2utd-2023-04-28-21-52-18
[2023-04-28 21:52:19] Uploaded result file: file-JmDO47plUVUMvcozhcCNQ2KI
[2023-04-28 21:52:20] Fine-tune succeeded

Job complete! Status: succeeded 🎉
Try out your fine-tuned model:

openai api completions.create -m davinci:ft-csstockt2utd-2023-04-28-21-52-18 -p <YOUR_PROMPT>
*/