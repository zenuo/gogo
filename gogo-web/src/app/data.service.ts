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

  public search(keyword: string, page: number=1): Observable<GogoResponse<ResultEntry[]>> {
    return this.http.get<GogoResponse<ResultEntry[]>>(`/api/search?q=${keyword}&p=${page}`)
  }
  getSuggestion(keyword: string) {
    return this.http.get<{
      lints: string[]
    }>(`/api/lint?q=${keyword}`)
  }
}

interface GogoResult {

}

export interface ResultEntry extends GogoResult {
  desc: string,
  url: string,
  name: string,
}

export interface GogoResponse<GogoResult> {
  error?: string,
  entries?: GogoResult,
}