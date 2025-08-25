-- KEYS[1] = active:item:{itemId}
-- KEYS[2] = hold:{holdingId}
-- KEYS[3] = hold:expirations
-- ARGV[1] = holdingId
-- ARGV[2] = ttlSec

local t = redis.call('TIME')
local now = tonumber(t[1])
local ttl = tonumber(ARGV[2])
local expireEpoch = now + ttl

local cur = redis.call('GET', KEYS[1])
if cur and cur ~= ARGV[1] then
  return {0, 'already_active'}
end

redis.call('SET', KEYS[1], ARGV[1], 'EX', ttl)
redis.call('SET', KEYS[2], '1', 'EX', ttl)
redis.call('ZADD', KEYS[3], expireEpoch, ARGV[1])

return {1, tostring(expireEpoch)}