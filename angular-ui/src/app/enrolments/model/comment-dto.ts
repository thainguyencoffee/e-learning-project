import {UserInfo} from "../../common/auth/user-info";

export class CommentDto {
  id?: number | null;
  content?: string | null;
  info?: UserInfo | null;
  attachmentUrls?: string[] | null;
  createdDate?: Date | null;
  lastModifiedDate?: Date | null;
  deleted?: boolean | null;

  constructor(data: Partial<CommentDto>) {
    Object.assign(this, data);
  }
}
