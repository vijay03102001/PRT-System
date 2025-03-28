import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ControlService {
  private freezeStatus = new BehaviorSubject<boolean>(false);

  toggleFreeze(): void {
    this.freezeStatus.next(!this.freezeStatus.value);
  }

  getFreezeStatus(): Observable<boolean> {
    return this.freezeStatus.asObservable();
  }
}
