import {UserInfo} from "../../../../common/auth/user-info";

export interface Comment {
  id: number;
  content: string;
  info: UserInfo;
  attachmentUrls?: string[];
  createdDate: Date;
  lastModifiedDate: Date;
}
