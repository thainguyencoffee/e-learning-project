export interface CourseRequest {
  id: number,
  type: string,
  status: string,
  resolved: boolean,
  resolvedBy: string,
  requestedBy: string,
  message: string,
  rejectReason: string,
  approveMessage: string,
}
