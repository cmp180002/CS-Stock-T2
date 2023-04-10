import requests
from pathlib import Path
from bs4 import BeautifulSoup

visited_urls = []


def web_crawler(starting_url):
    """
    Crawls through webpages of different starting URLs (defined down below
    as a list called 'starting_urls'). This function will output 2 text files
    Each text file will contain a list, separated by newlines, of links that the
    crawler was able to extract from the web page. The first text file ('urls_news.txt')
    contain links to articles about the stock market, investing, business, politics, etc.
    The second text file ('urls_stocks.txt') contains links to the stocks of the
    S&P 500 companies.
    :param starting_url: The starting URL that the crawler will use as a starting point
    to extract links from the webpage.
    :return: N/A
    """

    # Some preliminary setup before crawling through web pages
    r = requests.get(starting_url)
    data = r.text
    soup = BeautifulSoup(data, "html.parser")

    # Write URLs out to a text file. This file will contain links to news about
    # the stock market, business, investing, politics, etc.
    if starting_url != "https://www.stockmonitor.com/sp500-stocks/":

        with open("urls_news.txt", "a") as file:
            # Find the links from the starting URL
            urls_from_web_page = soup.find_all("a")

            for url in urls_from_web_page:
                url_string = str(url.get("href"))

                # Crawl through and save relevant URLs
                if url_string not in visited_urls:
                    # Need to edit the URLs for these sites
                    if starting_url == "https://lite.cnn.com":
                        if url_string.startswith('http'):
                            file.write(url_string + "\n")
                        else:
                            full_url_string = starting_url + url_string
                            file.write(full_url_string + "\n")
                        visited_urls.append(url_string)
                    elif starting_url == "http://68k.news/index.php?section=business&loc=US":
                        if url_string.startswith('article.php?loc=US&a='):
                            full_url_string = "http://68k.news/" + url_string
                            file.write(full_url_string + "\n")
                        visited_urls.append(url_string)
                    elif starting_url == "https://www.dailymail.co.uk/textbased/channel-605/index.html" \
                            or starting_url == "https://www.dailymail.co.uk/textbased/channel-603/index.html":
                        if url_string.startswith('/textbased/money'):
                            full_url_string = "https://www.dailymail.co.uk" + url_string
                            file.write(full_url_string + "\n")
                        visited_urls.append(url_string)
                    elif starting_url == "https://brutalist.report/source/yahoofinance":
                        if url_string.startswith("https://finance.yahoo.com/"):
                            file.write(url_string + "\n")
                        visited_urls.append(url_string)
                    elif starting_url == "https://www.cbc.ca/lite/news/business?sort=topics":
                        if url_string.startswith("/lite/story/"):
                            full_url_string = "https://www.cbc.ca" + url_string
                            file.write(full_url_string + "\n")
                        visited_urls.append(url_string)
                    elif starting_url == "https://investorplace.com/category/stock-picks/hot-stocks/" \
                            or starting_url == "https://investorplace.com/category/stock-picks/stocks-to-buy/" \
                            or starting_url == "https://investorplace.com/category/stock-picks/stocks-to-sell/" \
                            or starting_url == "https://investorplace.com/category/todays-market/" \
                            or starting_url == "https://investorplace.com/category/market-insight/":
                        if "/2023/" in url_string:
                            file.write(url_string + "\n")
                        visited_urls.append(url_string)
                    # All other websites' URLs should be good
                    else:
                        if url_string.startswith('http'):
                            file.write(url_string + "\n")
                        visited_urls.append(url_string)

        file.close()

    # Write URLs out to a text file. This file will contain links to the
    # stocks of the S&P 500 companies
    if starting_url == "https://www.stockmonitor.com/sp500-stocks/":

        with open("urls_stocks.txt", "a") as file:
            # Find the links from the starting URL
            urls_from_web_page = soup.find_all("a", href=True)

            for url in urls_from_web_page:
                url_string = str(url.get("href"))

                # Crawl through and save relevant URLs
                if url_string.startswith("/quote/"):
                    file.write("https://www.stockmonitor.com" + url_string + "\n")
                    visited_urls.append(url_string)
        file.close()


if __name__ == "__main__":
    starting_urls = ["http://68k.news/index.php?section=business&loc=US",
                     "https://brutalist.report/source/yahoofinance",
                     "https://brutalist.report/source/CNBC",
                     "https://investorplace.com/category/stock-picks/hot-stocks/",
                     "https://investorplace.com/category/stock-picks/stocks-to-buy/",
                     "https://investorplace.com/category/stock-picks/stocks-to-sell/",
                     "https://investorplace.com/category/todays-market/",
                     "https://investorplace.com/category/market-insight/",
                     "https://www.stockmonitor.com/sp500-stocks/"]

    # If you want to rerun the program, clear the files
    # before opening and writing into them again
    news_file = Path("urls_news.txt")
    stock_file = Path("urls_stocks.txt")
    if news_file.exists():
        open(news_file, "w").close()
    if stock_file.exists():
        open(stock_file, "w").close()

    # Iterate through list of URLs and crawl through links
    print("Crawling through URLs...")
    for url in starting_urls:
        web_crawler(url)
    print("Done!")
