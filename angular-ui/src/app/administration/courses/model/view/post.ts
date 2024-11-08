import {UserInfo} from "../../../../common/auth/user-info";
import {Comment} from "./comment";
import {Emotion} from "./emotion";

export interface Post {
  id: number;
  content: string;
  info: UserInfo;
  attachmentUrls?: string[];
  comments:Comment[];
  emotions:Emotion[];
  createdDate: Date;
  lastModifiedDate: Date;
  deleted: boolean;
}
