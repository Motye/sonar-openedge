DEFINE VARIABLE hMenuItem AS HANDLE NO-UNDO.

CREATE MENU-ITEM hMenuItem
  ASSIGN LABEL  = "XXX".

DEFINE VARIABLE hQuery  AS HANDLE NO-UNDO.
DEFINE VARIABLE hbCust  AS HANDLE NO-UNDO.

OPEN QUERY hQuery FOR EACH customer NO-LOCK WHERE customer.balance = 0.
CREATE BROWSE hbCust
   ASSIGN QUERY = QUERY hQuery:HANDLE.

DEFINE VARIABLE hSock AS HANDLE NO-UNDO.
CREATE SOCKET hSock.