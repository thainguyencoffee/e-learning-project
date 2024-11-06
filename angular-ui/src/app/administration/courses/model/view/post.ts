import {UserInfo} from "../../../../common/auth/user-info";

export interface Post {
  id: number;
  content: string;
  info: UserInfo;
  attachmentUrls?: string[];
  comments?:string;
  emotions?:string;
  createdDate: Date;
  lastModifiedDate: Date;
  deleted: boolean;
}
