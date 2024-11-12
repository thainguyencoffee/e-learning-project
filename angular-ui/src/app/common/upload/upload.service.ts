import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";

interface ObjectUrl {
  url: string
}

@Injectable({
  providedIn: 'root'
})
export class UploadService {
  http = inject(HttpClient);

  upload(file: File) {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<ObjectUrl>('/bff/api/upload', formData);
  }

  deleteAll(imageUrls: string[]) {
    return this.http.delete('/bff/api/upload/delete', { body: { urls: imageUrls }});
  }

}
