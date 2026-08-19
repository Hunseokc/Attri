RES=$(curl -s -X POST http://localhost:8080/api/users/login -H "Content-Type: application/json" -d '{"email":"test@test.com", "password":"password123"}')
TOKEN=$(echo $RES | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
if [ -z "$TOKEN" ]; then
    curl -s -X POST http://localhost:8080/api/users/signup -H "Content-Type: application/json" -d '{"email":"test@test.com", "username":"AtriinUser", "password":"password123"}' > /dev/null
    RES=$(curl -s -X POST http://localhost:8080/api/users/login -H "Content-Type: application/json" -d '{"email":"test@test.com", "password":"password123"}')
    TOKEN=$(echo $RES | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
fi

echo "Token: $TOKEN"

curl -v -X POST http://localhost:8080/api/feeds \
  -H "Authorization: Bearer $TOKEN" \
  -F "content=Hello world" \
  -F "creator=AtriinUser" \
  -F "type=art" \
  -F "tag=Daily" \
  -F "height=200" \
  -F "isCollab=false" \
  -F "hideCounts=false" \
  -F "disableComments=false"
