import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../environments/environment";

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

    return this.http.post<ObjectUrl>(environment.apiPath +'/api/upload', formData);
  }

  deleteAll(imageUrls: string[]) {
    return this.http.delete(environment.apiPath +'/api/upload/delete', { body: { urls: imageUrls }});
  }

}
