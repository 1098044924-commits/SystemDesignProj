export type AccountType = 'ASSET' | 'LIABILITY' | 'EQUITY' | 'INCOME' | 'EXPENSE';

export interface Account {
  id: number;
  name: string;
  code: string;
  type: AccountType;
  balance: number;
  currencySymbol: string;
  parentId: number | null;
}

export type DebitCredit = 'DEBIT' | 'CREDIT';

export interface Split {
  id?: number;
  accountId: number | null;
  accountName?: string;
  accountCode?: string;
  amount: number | null;
  direction: DebitCredit;
  quantity?: number | null;
  price?: number | null;
  memo?: string;
}

export interface Transaction {
  id: number;
  tradeDate: string;
  description?: string;
  reference?: string;
  cleared: boolean;
  splits: Split[];
}

export interface CreateTransactionRequest {
  tradeDate: string;
  description?: string;
  reference?: string;
  cleared: boolean;
  splits: Split[];
}

export interface PaginatedResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}


