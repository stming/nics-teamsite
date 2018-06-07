USE []
GO
/****** Object:  StoredProcedure [dbo].[news_article_mutiple]    Script Date: 06/18/2009 09:55:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO











-- 
-- This is the WWA Date secure version of this procedure and should be applied to production runtime
--
--



--*** test dates etc!!
CREATE PROCEDURE [dbo].[news_article_mutiple]
@SelectToDo varchar(50), --tell sp what to do
@numberArticles int, --number articles to show
@Quarter  varchar(50), --feed in quarter
@SY int, -- feed in year
@path  varchar(500), --feed in quarter
@Locale varchar(50), --feed in locale
@NewsType varchar(25) --the type of content


AS
BEGIN

    SET NOCOUNT ON;

-- set news type
DECLARE @NewsType2 nvarchar(20)

if (@NewsType = 'news')
BEGIN
SET @NewsType = 'news_releases'
SET @NewsType2 = ''
END
if (@NewsType = 'press')
BEGIN
SET @NewsType = 'press_releases'
SET @NewsType2 = ''
END
if (@NewsType = 'emergency')
BEGIN
SET @NewsType = 'emergency_releases'
SET @NewsType2 = ''
END
if (@NewsType = 'news+emergency')
BEGIN
SET @NewsType = 'news_releases'
SET @NewsType2 = 'emergency_releases'
END
-- end news type
    
---if path is set no need to bother with rest of query
if (@path <> '0')
BEGIN
SELECT  path
FROM dbo.metadata 
WHERE path = @path
AND NikonLocale =  @Locale 
AND news_type in (@NewsType, @NewsType2)
AND ((prod_wwa_date IS NOT NULL) AND (prod_wwa_date <= getDate()))
SET @Quarter = 'NA'
END


--SET VARIABLES TO WORK WITH--------------------------------------


--business variables for new, recent, archive
Declare @DateStart smalldatetime
Declare @DateEnd smalldatetime

SET @DateStart = DATEADD(mm, -8 + DATEDIFF(mm, GETDATE(), getDate()), getDate())
SET @DateEnd = DATEADD(mm, -3 + DATEDIFF(mm, GETDATE(), getDate()), getDate())

--debug
--SELECT @DateStart as dstart
--SELECT @DateEnd as dend

--BUSINESS RULES QUERIES--------------------------------------
--get default new results



if (@SelectToDo = 'New')
BEGIN
SELECT TOP(@numberArticles) path
FROM         dbo.metadata
WHERE  (((DateMade >= DATEADD(mm, -3 + DATEDIFF(mm, GETDATE(), getDate()), getDate())) AND (DateMade <= getDate()))
OR (keep_on_page = 1))
AND NikonLocale =  @Locale 
AND ((prod_wwa_date IS NOT NULL) AND (prod_wwa_date <= getDate()))
AND news_type in (@NewsType, @NewsType2)
ORDER BY DateMade DESC, NavName ASC
END


--get recent results
if (@SelectToDo = 'Recent')
BEGIN
SELECT path
FROM         dbo.metadata
WHERE     DateMade BETWEEN @DateStart AND @DateEnd
AND NikonLocale =  @Locale 
AND ((prod_wwa_date IS NOT NULL) AND (prod_wwa_date <= getDate()))
AND news_type in (@NewsType, @NewsType2)
ORDER BY DateMade DESC, NavName ASC
END

--get default quarter results
if (@SelectToDo = 'Archive')
BEGIN


--set variables if year or quarter is blank
--latest news
Declare @LatestArchive datetime

Set @LatestArchive =(SELECT TOP 1 DateMade
FROM dbo.metadata
WHERE   DateMade < @DateStart
AND NikonLocale =  @Locale 
AND ((prod_wwa_date IS NOT NULL) AND (prod_wwa_date <= getDate()))
AND news_type in (@NewsType, @NewsType2)
ORDER BY DateMade DESC)

--debug
--SELECT  @LatestArchive as latest

--set latest year
if (@SY = 0)
BEGIN
SET @SY = YEAR(@LatestArchive)
END


--make latest month
Declare @LatestQuarter datetime
if (@Quarter = '0')
BEGIN 

Set @LatestQuarter =(SELECT TOP 1 DateMade
FROM dbo.metadata
WHERE   DateMade < @DateStart
AND NikonLocale =  @Locale 
AND YEAR(DateMade) = @SY
AND ((prod_wwa_date IS NOT NULL) AND (prod_wwa_date <= getDate()))
AND news_type in (@NewsType, @NewsType2)
ORDER BY DateMade DESC)

END

--debug
--SELECT @LatestQuarter as lquarter


--set latest quarter
if (@Quarter = '0')
BEGIN
if ( Month(@LatestQuarter) BETWEEN 1 AND 3)
BEGIN
SET @Quarter = 'Q1'
END
if ( Month(@LatestQuarter) BETWEEN 4 AND 6)
BEGIN
SET @Quarter = 'Q2'
END
if ( Month(@LatestQuarter) BETWEEN 7 AND 9)
BEGIN
SET @Quarter = 'Q3'
END
if ( Month(@LatestQuarter) BETWEEN 10 AND 12)
BEGIN
SET @Quarter = 'Q4'
END
END



--debug
--SELECT @SY as latestyear
--SELECT @Quarter as latestquarter

--Q1
if ( @Quarter = 'Q1')
BEGIN
SELECT  path
FROM  dbo.metadata WHERE
Month(DateMade) BETWEEN 1 AND 3
AND Year(DateMade) = @SY
AND NIkonLocale =  @Locale 
AND DateMade < @DateStart
AND ((prod_wwa_date IS NOT NULL) AND (prod_wwa_date <= getDate()))
AND news_type in (@NewsType, @NewsType2)
ORDER BY DateMade DESC
END


--Q2
if ( @Quarter = 'Q2')
BEGIN
SELECT   path
FROM  dbo.metadata WHERE
Month(DateMade) BETWEEN 4 AND 6
AND Year(DateMade) = @SY
AND NikonLocale =  @Locale
AND DateMade < @DateStart 
AND ((prod_wwa_date IS NOT NULL) AND (prod_wwa_date <= getDate()))
AND news_type in (@NewsType, @NewsType2)
ORDER BY DateMade DESC
END


--Q3
if ( @Quarter = 'Q3')
BEGIN
SELECT  path
FROM  dbo.metadata WHERE
Month(DateMade) BETWEEN 7 AND 9
AND Year(DateMade) = @SY
AND NikonLocale =  @Locale 
AND DateMade < @DateStart
AND ((prod_wwa_date IS NOT NULL) AND (prod_wwa_date <= getDate()))
AND news_type in (@NewsType, @NewsType2)
ORDER BY DateMade DESC
END


--Q4
if ( @Quarter = 'Q4')
BEGIN
SELECT  path
FROM  dbo.metadata WHERE
Month(DateMade) BETWEEN 10 AND 12
AND Year(DateMade) = @SY
AND NikonLocale =  @Locale 
AND DateMade < @DateStart
AND ((prod_wwa_date IS NOT NULL) AND (prod_wwa_date <= getDate()))
AND news_type in (@NewsType, @NewsType2)
ORDER BY DateMade DESC
END

END


END
