1. Domain object chỉ chịu trách nhiệm thay đổi dữ liệu của nó, không thay đổi dữ liệu của các domain object khác.
2. SRP (Single Responsibility Principle): Mỗi domain object chỉ chịu trách nhiệm thực hiện một nhiệm vụ cụ thể.
3. DDD (Domain-Driven Design): Mỗi domain object phải có một phương thức chính để thực hiện nhiệm vụ của nó.
4. Encapsulation: Mỗi domain object phải ẩn thông tin của mình và chỉ cho phép truy cập thông qua các phương thức công khai.
5. Don't Tell Ask Principle: Mỗi domain object không nên yêu cầu thông tin từ các domain object rồi áp dụng logic xử lý, mà nên gửi thông tin đến các domain object khác để xử lý.