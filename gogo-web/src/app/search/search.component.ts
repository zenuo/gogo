import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { DataService, ResultEntry } from '../data.service';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  constructor(private route: ActivatedRoute,
    private dataService: DataService,
    private readonly title: Title,
    private router: Router) {
    
  }

  keyword?: string
  page?: number
  error?: string
  result?: ResultEntry[]

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      let keyword = params.get('q')
      let page = Number(params.get('p'))
      if (keyword == null || page == null) {
        this.router.navigate(['/'])
      } else {
        this.keyword = keyword
        this.page = page
        this.title.setTitle(`Gogo | ${keyword}`)
        this.dataService.search(keyword, page).subscribe(r => {
          this.error = r.error;
          this.result = r.result;
        })
      }
    })
  }
}
