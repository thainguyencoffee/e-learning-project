import {DestroyRef, Directive, inject, Input, OnInit, TemplateRef, ViewContainerRef} from '@angular/core';
import {AuthService} from './auth.service';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

/**
 * Directive IfAuthenticatedDirective giúp bạn linh hoạt trong việc hiển thị nội dung trong template dựa
 * trên trạng thái đăng nhập của người dùng*/
@Directive({
  selector: "[ifAuthenticated]",
  standalone: true
})
export class IfAuthenticatedDirective<T> implements OnInit {

  private authService = inject(AuthService);
  private templateRef = inject(TemplateRef<T>);
  private viewContainer = inject(ViewContainerRef)
  private destroyRef = inject(DestroyRef);

  public condition: boolean = false;
  public hasView: boolean = false;

  ngOnInit(): void {
    this.authService.isAuthenticated
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((isAuthenticated: boolean) => {
        const authRequired = isAuthenticated && this.condition;
        const unAuthRequired = !isAuthenticated && !this.condition;
        if ((authRequired || unAuthRequired) && !this.hasView) {
          this.viewContainer.createEmbeddedView(this.templateRef);
          this.hasView = true;
        } else if (this.hasView) {
          this.viewContainer.clear();
          this.hasView = false;
        }
      })
  }

  @Input() set ifAuthenticated(condition : boolean) {
    this.condition = condition;
  }

}
