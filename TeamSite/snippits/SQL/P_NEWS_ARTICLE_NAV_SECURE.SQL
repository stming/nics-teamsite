USE []
GO
/****** Object:  StoredProcedure [dbo].[spNav_NewsArticle]    Script Date: 06/19/2009 08:36:11 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO


-- =============================================
-- Author:		Emma Payne
-- Create date: August 2008
-- Description:	Gets the data required for the News Navigation
-- =============================================
-- Note:
-- Have re-factored to use the metadata table and also some columns
--
-- Note FP (26/05/2009): Added parameter to select either news releases or press releases 
--

CREATE PROCEDURE [dbo].[spNav_NewsArticle]
@Period varchar(50),
@Quarter varchar(50), -- selected quarter
@SY as int,
@SelectedID as varchar(250),
@Locale as varchar(50),
@NewsType as varchar(50)  -- select the news type, news or press

AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
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


SELECT @Period AS SelectedPeriod
SELECT @SelectedID as SelectedID


--set up dates if required for use in queries

Declare @DateStart smalldatetime
Declare @DateEnd smalldatetime



SET @DateStart = DATEADD(mm, -8 + DATEDIFF(mm, GETDATE(), getDate()), getDate())
SET @DateEnd = DATEADD(mm, -3 + DATEDIFF(mm, GETDATE(), getDate()), getDate())

--Declare @NewsType varchar(50)


if (@Period  = 'New' OR @Period = '' OR @Period is null)
BEGIN
SELECT     NavName, path, 'New' as Period
FROM         dbo.metadata
WHERE
	(DateMade >= DATEADD(mm, -3 + DATEDIFF(mm, GETDATE(), getDate()), getDate()) OR (keep_on_page = 1))
	AND DateMade <= getDate()
	AND NikonLocale =  @Locale 
	AND (prod_wwa_date IS NOT NULL AND prod_wwa_date <= getDate())
	AND news_type in (@NewsType, @NewsType2)
	ORDER BY DateMade DESC, NavName ASC

Set @Period  = 'New'

END

if (@Period  = 'Recent')
BEGIN
SELECT     NavName, path,  'Recent' as Period
FROM         dbo.metadata
WHERE     DateMade BETWEEN @DateStart AND @DateEnd
AND NikonLocale =  @Locale 
AND news_type in (@NewsType, @NewsType2)
AND (prod_wwa_date IS NOT NULL AND prod_wwa_date <= getDate())
ORDER BY DateMade DESC,  NavName ASC
END



if (@Period  = 'Archive')
BEGIN

--if selected year is blank or null make it current
if (@SY = '' or @SY is null)
BEGIN
Set @SY = Year(getdate())
END

-- show sel year
SELECT @SY as SelectedYear

-- get list of years first
SELECT DISTINCT TOP (100) PERCENT YEAR(DateMade) AS ArchiveYear
FROM         dbo.metadata
WHERE NikonLocale=  @Locale 
AND  DateMade < @DateStart 
AND news_type in (@NewsType, @NewsType2)
AND ((prod_wwa_date IS NOT NULL) AND (prod_wwa_date <= getDate()))
ORDER BY ArchiveYear DESC

-- get quarter
SELECT     NavName, path,  'Archive' as Period,  DateMade,
CASE 
WHEN (Month(DateMade) BETWEEN 1 AND 3) THEN 'Q1' 
WHEN (Month(DateMade) BETWEEN 4 AND 6) THEN 'Q2' 
WHEN (Month(DateMade) BETWEEN 7 AND 9) THEN 'Q3' 
WHEN (Month(DateMade) BETWEEN 10 AND 12) THEN 'Q4' 
END AS Quarter
FROM         dbo.metadata
WHERE   DateMade < @DateStart 
AND(YEAR(DateMade) = @SY)
AND NikonLocale =  @Locale 
AND news_type in (@NewsType, @NewsType2)
AND ((prod_wwa_date IS NOT NULL) AND (prod_wwa_date <= getDate()))
ORDER BY DateMade DESC


--if selected quarter is blank or null make it current
if (@Quarter = '' or @Quarter is null or @Quarter = '0')
BEGIN

SET  @Quarter = (


SELECT TOP (1) 
CASE WHEN (Month(DateMade) BETWEEN 1 AND 3) THEN 'Q1' 
WHEN (Month(DateMade) BETWEEN 4 AND 6) THEN 'Q2' 
WHEN (Month(DateMade) BETWEEN 7 AND 9) THEN 'Q3' 
WHEN (Month(DateMade) BETWEEN 10 AND 12) THEN 'Q4' 
END AS Quarter
FROM dbo.metadata
WHERE   DateMade < @DateStart 
AND(YEAR(DateMade) = @SY)
AND NIkonLocale =  @Locale 
AND news_type in (@NewsType, @NewsType2)
AND (prod_wwa_date IS NOT NULL AND prod_wwa_date <= getDate())
ORDER BY DateMade DESC
)
END


--get quarter back
SELECT @Quarter AS SelectedQuarter


END


END