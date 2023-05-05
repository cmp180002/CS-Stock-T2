/*
    Main.java
      Author: Charlotte Valentine

      This program provides flow control for the gpt-api component. It calls functions in Create-Finetunes.java
      to process the data scraper files produced by that component and then calls functions in Train-Model to
      actually train the model with the prompt-completion pairs.
*/
public class Main {
  public static void main(String[]args) {
    // Create Fine-tuning Data for the Day
    // finetuning_file is the filename of the created fine-tunes
    String finetuning_file = Create_Finetunes.ProduceFinetuningData();

    // Train the Model with the Fine-tunes
    /*
    if (!Train_Model.TrainModel(finetuning_file)) {
      System.out.println("Finetune Failed.");
    }    
    */
  }
}