export class CommentDto {
  content?: string | null;
  attachmentUrls?: string[] | null;

  constructor(data: Partial<CommentDto>) {
    Object.assign(this, data);
  }
}
