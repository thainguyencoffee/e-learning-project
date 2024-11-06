import {UserInfo} from "../../../common/auth/user-info"; // Import UserInfo nếu đã được định nghĩa

export class PostDto {
  id?: number | null;
  content?: string | null;
  info?: UserInfo | null;
  photoUrls?: string[] | null;
  createdDate?: Date | null;
  lastModifiedDate?: Date | null;

  constructor(data: Partial<PostDto>) {
    Object.assign(this, data);
    if (typeof this.photoUrls === 'string') {
      this.photoUrls = [this.photoUrls];
    } else if (!Array.isArray(this.photoUrls)) {
      this.photoUrls = [];
    }
  }
}
