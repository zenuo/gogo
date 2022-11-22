import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DataService {

  constructor(
    private http: HttpClient,
  ) { }

  public search(keyword: string, page: number=1): Observable<GogoReponse<ResultEntry[]>> {
    return this.http.get<GogoReponse<ResultEntry[]>>(`/api/search?q=${keyword}&p=${page}`)
  }
}

interface GogoResult {

}

export interface ResultEntry extends GogoResult {
  desc: string,
  url: string,
  name: string,
}

export interface GogoReponse<GogoResult> {
  error?: string,
  entries?: GogoResult,
}