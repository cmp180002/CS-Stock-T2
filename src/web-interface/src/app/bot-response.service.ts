import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class BotResponseService {
  normalwords: Array<string> =  [
    "The", "The", "A", "A", "An", "When", "For", "By", "Then", "Always", "Never", "Be", "And", "Of", "In", "To", "Have", "Too", "It",
    "At", "But", "That", "Not", "Can't", "Do", "As", "What", "Can", "Will", "If", "Get", "Would", "All", "Know", "Learn",
    "Serves", "Allows", "Requires", "Prohibits", "Forces"
  ];
  
  buzzwords: Array<string> =  [
    "Advertisement", "APR", "Asset", "ATM", "Bank", "Beneficiary", "Benefit", "Bill-payment", "Bimonthly", "Bond", 
    "Borrow", "Borrower", "Budget", "Business", "Buy", "Capital-gain", "Capital-loss", "Career", "Cash", "Check",
    "Claim", "Coin", "Coinsurance", "Collateral", "Commission", "Consumer", "Cosigner", "Cost", "Cost-effective",
    "Credit", "Creditworthy", "Cryptocurrency", "Debt", "Deductible", "Demand", "Dividend", "Donate", "Earn", 
    "Entrepreneur", "Fraud", "Gig", "Goal", "Goods", "Grant", "Income", "Inflation", "Insurance", "Insured", "Insurer", 
    "Interest", "Invest", "Investment", "Job", "Lend", "Lender", "Liability", "Liquidity", "Loan", "Medicaid", "Medicare", 
    "Money", "Mortgage", "Needs", "Occupation", "Overdraft", "Paper", "Pay", "Paycheck", "Payroll", "Phishing", "Policy", 
    "Policyholder", "Premium", "Prepayment", "Principal", "Profit", "Property", "Protect", "Raise", "Rebate", "Redlining", 
    "Repayment", "Return", "Risk", "Salary", "Save", "Savings", "Scam", "Scholarships", "Security", "Services", "Share", 
    "Spend", "Spoofing", "Stock", "Subscription", "Supply", "Tariff", "Taxes", "Term", "Tip", "Transaction", "Unbanked", 
    "Underbanked", "Value", "Volunteer", "Wage", "Wants", "Warranty"
  ]

  getRandom(array: Array<string>): string{
    return array[Math.floor(Math.random() * array.length)] 
  }

  getResponse(input: string): Promise<string>{
    return new Promise<string>((resolve, reject) => {
      setTimeout(() => {
        var inputWords = input.split(" ").map((str) => str.replace(/\W/g, ""));
        var baseString = "";
        var numberOfWords = Math.floor(Math.random() * 15) + 2 + inputWords.length;
        var lastWordNormal = Math.random() < .5;
        for(var i = 0; i < numberOfWords; i++){
          var useInputWord = Math.random() < Math.min(.4, .04 * inputWords.length)
          var newWord = lastWordNormal ? (!useInputWord ? this.getRandom(this.buzzwords): this.getRandom(inputWords)) : this.getRandom(this.normalwords);
          baseString += (i != 0) ? newWord.toLowerCase() : newWord;
          baseString += (Math.random() < .2 && i < numberOfWords - 3 && i > 3) ? "," : "";
          baseString += (i != numberOfWords - 1) ? " " : "";
          lastWordNormal = !lastWordNormal;
        }
        baseString += (Math.random() < .3) ? "!" : "."

        resolve(baseString)
      }, Math.random() * 2000 + 1000)
    });
  }

  constructor() { }
}
