# GPT-API Fine-tuning Component
Description | 
This component takes the data produced by the web scraper and extracts the stock market data and relevant news. It
uses the data is pulls from the scraper to create a JSONL formatted, timestamped fine-tune file in the fine-tuning
directory. Filenames take the form: fine-tuning-data_<timestamp>.jsonl

The fine-tuning file it creates represents the stock market data for the day you run the program. The file is ready
for fine-tuning with the OpenAI API:
	!openai api fine_tunes.create -t <fine-tune-filepath> -m <most-recent-model (from current-model.txt)>



Compilation | 
To compile, ensure you have Java 17+ installed.
You can either run the makefile in this directory:
			make

Or run the compilation command manually:
			javac Main.java Create_Finetunes.java Train_Model.java


This will produce many class files, but the one you want to run the component with it Main.class



Execution | 
**Ensure you have run the data scraping component before this, or the fine-tuning generation won't work!

To run the component, you can either run the script in this directory:
			Generate_Finetunes.sh

Or manually run the program (after manually running the data scraper):
			java Main