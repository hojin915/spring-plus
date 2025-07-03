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

#### User Nickname 검색
처리 시간 측정 방식 : UserController 에서 `리턴 직전 시간 - 시작 시간`  
10회 요청 후 최댓값/최솟값 제외한 평균 시간으로 계산  

초기 방식
```java
// Service
Page<User> users = userRepository.findAllByNickname(nickname, pageable);

// Repository
@Query("SELECT u FROM User u WHERE u.nickname = :nickname")
Page<User> findAllByNickname(@Param("nickname")String nickname, Pageable pageable);
```
걸린 시간 :  
766, `658`, `952`, 785, 792, 775, 659, 787, 905, 884  
Average : 794(milliseconds)

개선(v1) - DTO Projection(Constructor-based)
```java
// Service
public Page<UserSearchResponse> searchUser(int page, int size, String nickname) {
    Pageable pageable = PageRequest.of(page - 1, size);

    return userRepository.findAllByNickname(nickname, pageable);
}

// Repository
@Query("SELECT new org.example.expert.domain.user.dto.response.UserSearchResponse(u) " +
        "FROM User u " +
        "WHERE u.nickname = :nickname")
Page<UserSearchResponse> findAllByNickname(@Param("nickname")String nickname, Pageable pageable);
```
걸린 시간 :  
`662`, 691, 780, 772, 802, 758, 703, `913`, 841, 819  
Average : 771(milliseconds)

개선(v2) - 리턴 타입 변경(Page -> Slice)  
```java
// Service
public Slice<UserSearchResponse> searchUser(int page, int size, String nickname) {
    Pageable pageable = PageRequest.of(page - 1, size);

    return userRepository.findAllByNickname(nickname, pageable);
}

// Repository
@Query("SELECT new org.example.expert.domain.user.dto.response.UserSearchResponse(u) " +
        "FROM User u " +
        "WHERE u.nickname = :nickname")
Slice<UserSearchResponse> findAllByNickname(@Param("nickname")String nickname, Pageable pageable);
```
걸린 시간 :  
6, 34, 410, 735, `796`, `5`, 20, 763, 776, 716  
Average : 433(milliseconds)

시간이 급격하게 차이나는 이유  
1. 10개의 입력값의 길이가 1~5글자, 1~5글자
2. 1~5글자 nickname 을 자동생성해서 글자수가 적을수록 일치할 확률이 높다
3. 일치하는 값이 많아 페이지에 필요한 개수를 빨리 채우기 때문에 글자수가 적을수록 빠른구조
4. 일치하는 경우가 없을 때 끝까지 탐색하기 때문에 오래걸린다
5.  하지만 Page 리턴에 필요한 Count 쿼리가 없기 때문에 상대적으로 짧음

개선(v3) - nickname 컬럼 index 처리  
```sql
CREATE INDEX idx_users_nickname ON users (nickname);
```
걸린 시간 :  
3, 4, 4, 3, 3, 8, `9`, 4, `2`, 7  
Average : 4.5(milliseconds)

이렇게 빠르면 굳이 Slice 를 사용할 필요가 있을까?

개선(v4) - Page 리턴으로 복귀(index 처리 유지)  
걸린 시간 :  
 `9`, 7, 5, `2`, 3, 7, 6, 6, 3, 3  
Average : 5(milliseconds)

개선(v5) - Projection 정상화
- v1에서 DTO Projection 을 통해서 Page 매핑하는 절차를 건너뛰었다
- 하지만 Query 를 확인한 결과 Projection 이 제대로 적용되지 않고 User 전체를 SELECT 하고 있었다
- DTO 생성에 필요한 데이터멘 SELECT 하도록 수정
```java
// Repository
    @Query("SELECT new org.example.expert.domain.user.dto.response.UserSearchResponse(u.id, u.nickname, u.email) " +
            "FROM User u " +
            "WHERE u.nickname = :nickname")
    Page<UserSearchResponse> findAllByNickname(@Param("nickname")String nickname, Pageable pageable);
```
걸린 시간 :  
`7`, 4, 4, `2`, 4, 7, 6, 3, 3, 2  
Average : 4.1(milliseconds)

#### 요약 / 정리표
![Search User 성능 정리표.png](image/Search%20User%20%EC%84%B1%EB%8A%A5%20%EC%A0%95%EB%A6%AC%ED%91%9C.png)