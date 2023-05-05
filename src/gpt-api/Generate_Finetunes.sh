#!/bin/bash

# Run the web crawler/web scraper
cd ../data-scraper

#  Ensure you have bs4/requests (required Python Libraries)
python -m pip install bs4
python -m pip install requests

python WebCrawler_StockMarket.py
python WebScraper_StockMarket.py

# Return to gpt-api and run the fine-tuning generation program
cd ../gpt-api

java Main