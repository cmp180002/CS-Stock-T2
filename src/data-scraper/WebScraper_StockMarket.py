import http.client
import urllib
from pathlib import Path
from urllib import request
from urllib.request import Request, urlopen
from bs4 import BeautifulSoup


# Scrapes through the given url to extract text within <p> tags.
def scrape_news(url):
    # Replacing spaces in urls with "%20" to prevent potential issues accessing websites.
    # Also making the scraper appear to be from a web browser to prevent websites from blocking the scraper.
    # And finally reading the html page.
    try:
        url.replace(" ", "%20")
        req = Request(url, headers={"User-Agent": "Mozilla/5.0"})
        html = urlopen(req).read()
    # If an error is run into, then the scraper should return and skip the website.
    except (urllib.error.HTTPError, urllib.error.URLError, http.client.InvalidURL) as e:
        return " "

    # Using BeautifulSoup to parse the webpage.
    soup = BeautifulSoup(html, "html.parser")

    # Obtaining all the <p> tags and adding them to a list.
    p_tags = soup.find_all("p")
    p_text = []
    for elem in p_tags:
        if elem.string is not None:
            p_text.append(elem.string)

    # Appending to the existing text file. Creates a new file if it doesn't exist already.
    with open("scraped_news.txt", "a", encoding="utf-8") as g:
        # Writing the url and then paragraph information afterwards.
        g.write(url)
        for text in p_text:
            g.write(text + "\n")
        g.write("\n")


# Scrapes through the given url to extract stock information contained with a table.
def scrape_stocks(url):
    # Requesting the given webpage.
    html = urllib.request.urlopen(url)
    # Using BeautifulSoup to parse the webpage.
    soup = BeautifulSoup(html, "html.parser")

    # Obtaining the company name for writing in a file.
    name = soup.find("h2", {"class": "name"})

    # Searching the div and span that contains current price and changes of specific stocks.
    div_current_price = soup.find("div", {"class": "price-and-changes"})
    if div_current_price is not None:
        span_current_price = div_current_price.find_all("span")
    else:
        return " ", " ", " "

    current_price = []

    # Combines the current prices and other relevant information into a list.
    for elem in span_current_price:
        if elem.string:
            current_price.append(elem.string)

    # Extracting the <th> tags and adding them to a list.
    hist_table = soup.find("table", {"class": "table table-hist-data table-condensed"})
    th_tags = hist_table.find_all("th")
    th_text = []
    for elem in th_tags:
        if elem.string:
            th_text.append(elem.string)

    # Extracting the <td> tags and adding them to a list.
    td_tags = hist_table.find_all("td")
    td_text = []
    for elem in td_tags:
        if elem.string:
            td_text.append(elem.string)

    # Creating a list that combines both the th_text and td_text
    full_table = th_text + td_text

    # Appending to the existing text file. Creates a new file if it doesn't exist already.
    with open("scraped_stocks.txt", "a", encoding="utf-8") as g:
        # Writing the url, stock_name, and current_price to the file.
        g.write(url + "\n")
        g.write(name.text + "\n")
        g.write(" ".join(current_price) + "\n")

        count = 0

        # Formatting the output for ease of use.
        for text in full_table:
            if count == 6:
                g.write("\n")
                count = 0
            g.write(str(text) + " ")

            count += 1

        g.write("\n\n")


if __name__ == '__main__':
    print("Running the scraper..")

    scraped_news_file = Path("scraped_news.txt")
    scraped_stocks_file = Path("scraped_stocks.txt")

    if scraped_news_file.exists():
        open(scraped_news_file, "w").close()
    if scraped_stocks_file.exists():
        open(scraped_stocks_file, "w").close()

    # Reading the "urls_news.txt" to begin scraping all the urls.
    with open("urls_news.txt", "r", encoding="utf-8") as f:
        # Reads the file all at once into a list.
        base_urls = f.readlines()
        urls = []

        # Removing the newline character that is contained on all the links.
        for url in base_urls:
            urls.append(url.replace("\n", ""))

        # Writing the scraped news test to a file to check output.
        for url in urls:
            # These two links in particular caused issues, but no other ones did.
            # So they are put into a list and if encountered, then the loop skips and continues to the next iteration.
            bad_urls = ["https://corporate.comcast.com/values/integrity",
                        "https://feedpress.me/link/20202/16047374/musk-experts-urge-pause-on-training-of-ai-systems-that-can-outperform-gpt-4"]
            if url in bad_urls:
                continue

            # Running the scrape_news() function
            scrape_news(url)

    # Reading the "urls_stocks.txt" to begin scraping all the urls.
    with open("urls_stocks.txt", "r", encoding="utf-8") as f:
        # Reads the file all at once into a list.
        base_urls = f.readlines()
        urls = []

        # Removing the newline character that is contained on all the links.
        for url in base_urls:
            urls.append(url.replace("\n", ""))

        # Writing the scraped news test to a file to check output.
        for url in urls:
            # Running the scrape_stocks function.
            scrape_stocks(url)

    print("Done.")
