export interface UserInfo {
  firstName: string,
  lastName: string,
  username: string,
}

export interface Comment {
  id: number,
  content: string,
  info: UserInfo,
  attachmentUrls?: string[],
  createdDate: string,
  lastModifiedDate: string,
  isEditing?: boolean | false,
}

export interface Emotion {
  id: number,
  username: string,
  createdDate: string,
}

export interface Post {
  id: number,
  content: string,
  info: UserInfo,
  attachmentUrls?: string[],
  comments?: Comment[],
  emotions?: Emotion[],
  createdDate: string,
  lastModifiedDate: string,
}
