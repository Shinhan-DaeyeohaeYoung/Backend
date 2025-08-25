-- KEYS[1]=active:item:{itemId}
-- KEYS[2]=hold:{holdingId}
-- KEYS[3]=hold:expirations
-- ARGV[1]=holdingId

local cur = redis.call('GET', KEYS[1])
if cur == ARGV[1] then
  redis.call('DEL', KEYS[1])
end
redis.call('DEL', KEYS[2])
redis.call('ZREM', KEYS[3], ARGV[1])
return {1, 'reverted'}