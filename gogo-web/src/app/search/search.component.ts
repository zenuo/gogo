import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { SearchService } from 'src/services/search';
import { DataService, ResultEntry } from '../data.service';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.less']
})
export class SearchComponent implements OnInit {

  formGroup = new FormGroup({
    keyword: new FormControl('')
  });

  constructor(private route: ActivatedRoute,
    private dataService: DataService,
    private searchService: SearchService,
    private router: Router,
    private readonly title: Title,
  ) {

  }

  keyword?: string
  page?: number
  error?: string
  result?: ResultEntry[]
  suggestions: string[] = []
  showedSuggestions: {
    isHistory: boolean
    value: string
  }[] = []
  suggestionsVisible = false
  fetchTimer?: any

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      let keyword = params.get('q')
      let page = Number(params.get('p') || 1)
      if (keyword == null || page == null) {
        this.router.navigate(['/'])
      } else {
        this.keyword = keyword
        this.formGroup.setValue({keyword: keyword})
        this.page = page
        this.title.setTitle(`Gogo | ${keyword}`)
        this.dataService.search(keyword, page).subscribe(r => {
          this.error = r.error;
          this.result = r.result;
        })
      }
      this.getSuggestion(keyword as any)
    })
    
  }
  onFocus() {
    this.suggestionsVisible = true
  }
  onBlur() {
    setTimeout(() => {
      this.suggestionsVisible = false
    }, 200)
  }
  refreshShowedSuggestions() {
    this.showedSuggestions = this.searchService.getShowSuggestions(this.formGroup.value.keyword as any, this.suggestions, this.searchService.historyList)
  }
  getSuggestion(keyword: string) {
    this.dataService.getSuggestion(keyword).subscribe(r => {
      this.suggestions = (r.result || []).map(s => s.replace(/<[^>]+>/g, ''))
      this.refreshShowedSuggestions()
    })
  }
  onChange(event: any) {
    const keyword = event.target.value
    this.suggestionsVisible = true
    this.refreshShowedSuggestions()
    if (keyword) {
      if (this.fetchTimer) {
        clearTimeout(this.fetchTimer)
      }
      this.fetchTimer = setTimeout(() => {
        this.getSuggestion(keyword)
      }, 500)
    }
  }
  onSelectSuggestions(suggestion: string) {
    this.searchService.search(suggestion)
  }

  onSubmit() {
    const keyword = this.formGroup.value.keyword
    this.searchService.search(keyword as any)
    this.suggestionsVisible = false
  }
}
