# MaziRecorderAndroid



## API Examples

```
# POST api/interviews/ (returns interview: { _id : xxx, ...})
{
  text: 'Synopsis Lorem ipsum',
  name: 'Peter'
  role: 'Designer'
}

# POST api/file/upload/image/:interviewId (returns interview)
FILES['file'] = file

# POST api/attachments/ (returns attachment: { _id : xxx, ...})
{
	text: 'Question text',
	tags: ['test1' , 'test2'],
	interview: interviewId // obtained after creating the interview
}

# POST api/upload/attachment/:attachmentId (returns attachment)
FILES['file'] = file
```