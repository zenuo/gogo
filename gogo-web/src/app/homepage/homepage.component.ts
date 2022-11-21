import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent implements OnInit {

  title = '勾勾';
  formGroup = new FormGroup({
    keyword: new FormControl('')
  });

  constructor(
    private router: Router
  ) { }

  ngOnInit(): void {
  }

  onSubmit() {
    this.router.navigate(['search'],
    {
      queryParams: {
        "q": this.formGroup.value.keyword
      }
    })
  }

}
