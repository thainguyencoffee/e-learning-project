export class ApproveRequestDto {
  approveType?: string | null;
  approveMessage?: string | null;
  approveBy?: string | null;

  constructor(data: Partial<ApproveRequestDto>) {
    Object.assign(this, data);
  }

}
