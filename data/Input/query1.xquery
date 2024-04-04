declare option output:method "xml";
declare option output:indent "yes";

let $results :=
  for $question in doc("anime/Posts.xml")//row
  let $postId := $question/@Id/string()
  let $postTypeId := $question/@PostTypeId/string()
  let $acceptedAnswerId := $question/@AcceptedAnswerId/string()
  let $creationDate := $question/@CreationDate/string()
  let $score := $question/@Score/string()
  let $viewCount := xs:integer($question/@ViewCount)
  let $body := $question/@Body/string()
  let $ownerUserId := $question/@OwnerUserId/string()
  let $lastActivityDate := $question/@LastActivityDate/string()
  let $title := $question/@Title/string()
  let $tags := $question/@Tags/string()
  let $answerCount := $question/@AnswerCount/string()
  let $commentCount := $question/@CommentCount/string()
  let $contentLicense := $question/@ContentLicense/string()
  where exists($postId) and exists($postTypeId) and exists($creationDate) and exists($score) and exists($viewCount)
  order by $viewCount descending
  return <post>
            <Id>{$postId}</Id>
            <PostTypeId>{$postTypeId}</PostTypeId>
            <AcceptedAnswerId>{$acceptedAnswerId}</AcceptedAnswerId>
            <CreationDate>{$creationDate}</CreationDate>
            <Score>{$score}</Score>
            <ViewCount>{$viewCount}</ViewCount>
            <Body>{$body}</Body>
            <OwnerUserId>{$ownerUserId}</OwnerUserId>
            <LastActivityDate>{$lastActivityDate}</LastActivityDate>
            <Title>{$title}</Title>
            <Tags>{$tags}</Tags>
            <AnswerCount>{$answerCount}</AnswerCount>
            <CommentCount>{$commentCount}</CommentCount>
            <ContentLicense>{$contentLicense}</ContentLicense>
         </post>

return <posts>{$results}</posts>
