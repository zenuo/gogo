import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { DataService } from 'src/app/data.service';
import { addHistory, getHistoryList } from './history';

@Injectable({
  providedIn: 'root',
})
export class SearchService {
  historyList: string[] = [];
  constructor(private router: Router, private dataService: DataService) {
    this.historyList = getHistoryList();
  }

  search(keyword: string) {
    if (keyword) {
      addHistory(keyword);
      this.historyList = getHistoryList();
    }
    this.router.navigate(
      ['search'],

      {
        queryParams: {
          q: keyword,
        },
      }
    );
  }
  getSuggestion(keyword: string) {
    return this.dataService.getSuggestion(keyword);
  }
  getShowSuggestions(keyword: string, suggestions: string[], historyList: string[]) {
    const mapFn = (s: string, isHistory = true) => {
      return {
        isHistory,
        value: s,
      }
    }
    if (!keyword) {
      return historyList.map(s => mapFn(s))
    }
    const historySuggestions = historyList.filter((s) => s.includes(keyword))
    return historySuggestions.slice(0, 5).map(s => mapFn(s)).concat(suggestions.filter(s => !historySuggestions.includes(s)).map(s => mapFn(s, false))).slice(0, 10)
  }
}
