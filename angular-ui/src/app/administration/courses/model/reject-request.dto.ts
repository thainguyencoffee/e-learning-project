export class RejectRequestDto {
  rejectType?: string | null;
  rejectCause?: string | null;
  rejectBy?: string | null;

  constructor(data: Partial<RejectRequestDto>) {
    Object.assign(this, data);
  }

}
