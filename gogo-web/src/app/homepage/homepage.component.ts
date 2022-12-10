import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { addHistory } from 'src/services/history';
import { SearchService } from 'src/services/search';

@Component({
  selector: 'app-homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.less']
})
export class HomepageComponent implements OnInit {

  title = '勾勾';
  formGroup = new FormGroup({
    keyword: new FormControl('')
  });

  constructor(
    private searchService: SearchService
  ) { }

  ngOnInit(): void {
  }

  onSubmit() {
    const keyword = this.formGroup.value.keyword
    this.searchService.search(keyword as any)
  }

}
