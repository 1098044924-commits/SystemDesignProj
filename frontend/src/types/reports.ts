export interface BalanceSheetItem {
  accountCode: string;
  accountName: string;
  accountType: string;
  amount: number;
}

export interface BalanceSheetResponse {
  assets: BalanceSheetItem[];
  liabilities: BalanceSheetItem[];
  equity: BalanceSheetItem[];
}

export interface IncomeStatementItem {
  accountCode: string;
  accountName: string;
  accountType: string;
  amount: number;
}

export interface IncomeStatementResponse {
  incomes: IncomeStatementItem[];
  expenses: IncomeStatementItem[];
}

export interface TrialBalanceRow {
  accountCode: string;
  accountName: string;
  debit: number;
  credit: number;
}

export interface TrialBalanceResponse {
  rows: TrialBalanceRow[];
  totalDebit: number;
  totalCredit: number;
}












