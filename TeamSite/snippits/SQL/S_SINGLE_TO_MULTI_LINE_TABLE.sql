SELECT VP1.ID
      ,VP1.NIKONLOCALE
      ,VP1.PROD_SHORT_CODE
      ,SBQ1.PROD_ACCESSORY_OF
      ,(SELECT TOP(1) VP2.PROD_ID, VP2.ID 
        FROM V_PRODUCT VP2 
        WHERE VP2.PROD_SHORT_CODE = SBQ1.PROD_ACCESSORY_OF 
        AND VP2. NIKONLOCALE = VP1.NIKONLOCALE) AS P_ID
FROM V_PRODUCT VP1
CROSS JOIN(
SELECT  ID
       ,PROD_ID
       ,NIKONLOCALE
       ,PROD_SHORT_CODE
       ,SUBSTRING(PROD_ACCESSORY_OF, N, CHARINDEX(':', PROD_ACCESSORY_OF + ':',N) - N) AS PROD_ACCESSORY_OF
       ,N + 1 - LEN(REPLACE(LEFT(PROD_ACCESSORY_OF, N), ':', '' )) AS INDX
FROM V_PRODUCT AS VP
CROSS JOIN (SELECT number
            FROM master..spt_values
            WHERE type = 'P'
            AND number BETWEEN 1 AND 100) AS Numbers(n)
WHERE SUBSTRING(':' + prod_accessory_of, n, 1) = ':'
AND n < LEN(prod_accessory_of) + 1) AS SBQ1
WHERE VP1.ID = SBQ1.ID 
GO