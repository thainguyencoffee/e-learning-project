export interface FieldConfiguration {
  name?: string,
  type: string,
  label?: string,
  placeholder?: string,
  options?: Record<string, string> | Map<number, string>,
}
