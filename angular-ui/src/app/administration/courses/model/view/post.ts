import {UserInfo} from "../../../../common/auth/user-info";

export interface Post {
  id: number;
  content: string;
  info: UserInfo;
  photoUrls?: string[];
  createdDate: Date;
  lastModifiedDate: Date;
  deleted: boolean;// Array for multiple image URLs
}
