# SPRING PLUS

### AWS
#### EC2 설정
Elastic IP 주소 사용중(릴리스 예정)
![ec2 설정.png](image/ec2%20%EC%84%A4%EC%A0%95.png)
#### 인스턴스에서 실행
빌드파일을 EC2 인스턴스로 옮겨서 실행
![인스턴스에서 실행.png](image/%EC%9D%B8%EC%8A%A4%ED%84%B4%EC%8A%A4%EC%97%90%EC%84%9C%20%EC%8B%A4%ED%96%89.png)
#### RDS 연결 확인
EC2 인스턴스에서 실행한 서버에 요청했을 때 연결한 RDS 에 데이터 추가
![rds 연결.png](image/rds%20%EC%97%B0%EA%B2%B0.png)
#### 상태 검사
상태 검사 API: GET `{URL}/helath`  
API 확인 : http://13.124.221.157/health
![상태 검사 설정.png](image/%EC%83%81%ED%83%9C%20%EA%B2%80%EC%82%AC%20%EC%84%A4%EC%A0%95.png)
#### S3 버킷
유저 프로필 이미지 저장  
`dirName/userId{확장자}` 형식으로 저장  
Public 접근 불가능, API 요청시 PresignedURL 리턴
![s3 버킷.png](image/s3%20%EB%B2%84%ED%82%B7.png)