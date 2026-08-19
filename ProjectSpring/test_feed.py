import requests

# 1. Signup
s = requests.Session()
s.post('http://localhost:8080/api/users/signup', json={
    'email': 'test@test.com',
    'username': 'AtriinUser',
    'password': 'password123'
})

# 2. Login
res = s.post('http://localhost:8080/api/users/login', json={
    'email': 'test@test.com',
    'password': 'password123'
})
token = res.json().get('accessToken')

# 3. Post Feed
res = s.post(
    'http://localhost:8080/api/feeds',
    headers={'Authorization': 'Bearer ' + token},
    data={
        'content': 'Hello world',
        'creator': 'AtriinUser',
        'type': 'art',
        'tag': 'Daily',
        'height': '200',
        'isCollab': 'false',
        'hideCounts': 'false',
        'disableComments': 'false'
    }
)
print("Status:", res.status_code)
print("Response:", res.text)
