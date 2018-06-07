use [];
go
CREATE FUNCTION dbo.GetLocalisedCommunityAssetOrder (@locale CHAR(8))
RETURNS TABLE
AS
RETURN (
select vpath, id, replicantOrder 
	from communityAssetGroup 
	where (vpath like '%\flickrorder%' or vpath like '%\youtubeorder%') and vpath like '%' + @locale + '%'
union
select vpath, id, replicantOrder 
	from communityAssetGroup 
	where (vpath like '%\flickrorder%' or vpath like '%\youtubeorder%') and vpath like '%en_EU%' and id not in 
	(select id from communityAssetGroup where (vpath like '%\flickrorder%' or vpath like '%\youtubeorder%') and vpath like '%' + @locale + '%')
);
go