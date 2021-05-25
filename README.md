# Project C.R.E.A.M. - SoPra Group 21

## Introduction

### Aim of the platform
Our platform allows users to simulate trading stocks, opening long and short positions, using real time financial data.
Each user can create several portfolios, each with a starting balance of 100,000 CHF, and trade stocks in the main
US markets, the Frankfurt market and the Swiss Exchange. Price, as well as the currency exchange rate, are fetched in
real time.

### Additional features

#### Real-time collaboration
You can collaborate with other users by letting them join one of your portfolios via the join code.
Each portfolio also has a dedicated trader's chat.

#### Leaderboard
The leaderboard will show the top-performing portfolios, you will be able to examine them in detail by clicking on them.

#### User profiles
By clicking on a username in the portfolio page, you will be redirected to a user's profile page, where you can see
the user's information as well as the portfolios they are trading in.

## Technologies

We are using Java for the backend and JavaScript and React for the frontend.
The main other frameworks we are using are Spring, Rest, Javamail (for password recovery),
Heroku PostGREs (persistent storage).
The financial data is provided by AlphaVantage, who kindly gave us a free license for this project.

## High level components (backend)

### Financial
There are three main components dealing with the financial logic of the application. The [FinanceService class](https://github.com/sopra-fs21-group-21/Server/blob/master/src/main/java/ch/uzh/ifi/hase/soprafs21/service/FinanceService.java)
fetches data from AlphaVantage upon request from other parts of the program.
The [PositionService class](https://github.com/sopra-fs21-group-21/Server/blob/master/src/main/java/ch/uzh/ifi/hase/soprafs21/service/PositionService.java)
deals with all open positions (long and short on a stock), it is responsible for updating the prices, which it does by
updating a position per second constantly and other position-related tasks.
The [PortfolioService class] is responsible for handling anything portfolio related. Opening and closing a position,
adding and subtracting cash, computing the total value of a portfolio. Note that this class does not directly access
financial data or update positions, as that is delegated to the position class, which felt like better encapsulation.

### User & chat

## Launch and deployment

The project has continuous deployment, meaning that as soon as a commit is pushed to the main branch, the tests are
executed, and if all tests pass, the project is then deployed to Heroku (host) and to SonarCloud (for code analysis).
Therefore, just push, everything else will follow.

One caution note goes to the database structure. Because we use persistent storage, one should be careful not to break
the database logic when updating the code.

If you run the application locally the port is localhost:8080.

## Flow (illustration)

Log in into the application. You will see your portfolios and buttons to create or join new ones on the left, and the leaderboard
on the right. If you want to access your profile and update information, the top-right menu is what you are looking for.

If you click on a portfolio, you will see it's portfolio page. If you are a trader, you can open positions and chat.
if not, just enjoy the view.

## Roadmap

Some features that would make nice additions.

### Charts
Yes, we know. You say "finance" and people think charts. Our application does not have charts, but they would fit well.
You might add charts that show variations of a portfolio value, capital and cash across time.
Note that charts for a stock price history would be hard to include, as AlphaVantage does not provide historical data
for all stocks.

### Currency trading
Why not also trade currencies, both standard and crypto? We have the data, and the backend is readily extensible, as we
foresaw this possibility.

### Other markets
In a globalized world, most things finance still seem to focus on the United States (even here in Europe). Most of the data, historical and realtime,
is on US stocks. Most educational content and research is US-centered. We wanted to give our application a distinctly European touch,
which is why we are supporting two european (europe != EU) exchanges and using swiss francs as the main currency. It
would be nice to include markets from the rest of the world (though data is scarce and expensive).

## Authors

Alessandro Vanzo - github.com/alessandrovanzo

## License

MIT License

Copyright (c) [2021] [Alessandro Vanzo]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
