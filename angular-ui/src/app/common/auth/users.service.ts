import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {UserInfo} from "./user-info";

@Injectable({
  providedIn: 'root',
})
export class UsersService {
  http = inject(HttpClient);
  resourcePath = '/bff/api/users'

  searchByUsername(username: string, exact?: boolean): Observable<UserInfo[]> {
    const isExact = exact || false;
    return this.http.get<UserInfo[]>(`${this.resourcePath}/search`, {
      params: {
        username: username,
        exact: isExact.toString()
      }
    });
  }

}
