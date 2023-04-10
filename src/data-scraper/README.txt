FILES IN PROJECT:

	WebCrawler_StockMarket.py:
	The goal of this program is to collect a bunch of URLs for the scraper to 
	extract information out of. This program outputs two files: 'urls_news.txt' and
	'urls_stocks.txt'. 'urls_news.txt' is a list of URLs collected
	from different news sites such as Yahoo Finance, CNBC, Investor Place, etc. 
	These URLs link to different articles about the stock market, business, investing,
	politics, etc. 'urls_stocks.txt' is a list of URLs collected from www.stockmonitor.com.
	Each URL is a link to one of the stocks of the S&P 500 companies.

	WebScraper_StockMarket.py:
	The goal of this program is to extract information from the webpages of the URLs
	provided by the web crawler. This program outputs two files: 'scraped_news.txt'
	and 'scraped_stocks.txt'. 'scraped_news.txt' mainly extracts the paragraphs of text
	contained in the article of the different news sites. There might be some
	extraneous info, but the main goal was to extract the actual article from the webpage.
	'scraped_stocks.txt' extracts information regarding the stocks of the various S&P 500 
	companies, including the company's name, the price change/percentage change, as 
	well as the opening, high, low, close, and volume numbers for the past few days.


FORMAT OF OUTPUT FILES:
	
	'urls_news.txt' and 'urls_stocks.txt' each simply contain a list, separated 
	by newlines, of the URLs to either the news articles or stock data respectively.

	'scraped_news.txt' is formatted the following way:

		<URL of news article>
		[paragraphs of text extracted from the article]

		<URL of news article>
		[paragraphs of text extracted from the article]

		.
		.
		.
		
		<URL of news article>
		[paragraphs of text extracted from the article]

	'scraped_stocks.txt' is formatted the following way:

		<URL to company's stock>
		[company's stock info]

		<URL to company's stock>
		[company's stock info]

		.
		.
		.
		
		<URL to company's stock>
		[company's stock info]


HOW TO RUN PROGRAMS:
	
	We just ran the programs through an IDE; we specifically used PyCharm.
	You must run WebCrawler_StockMarket.py before you run WebScraper_StockMarket.py.

	It also might take a while to run the programs. From our experience, it can take
	~15 minutes to run both the crawler and the scraper.


IMPORTANT NOTE:
	
	Because the web crawler extracts URLs in real time, the exact information gathered 
	by the scraper changes on pretty much an everyday basis.

