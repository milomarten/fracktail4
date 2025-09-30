# Fracktail Dice Expressions
The `/roll` command in Fracktail is a powerful tool,
that can do a number of math operators. It can be tricky
to understand, and impossible to explain all the nuances
within a short text description. This README file will
explain all the ins and outs of the command.

## Operators
All major operators are supported, and all formatting of equations
is written in the same way as basic math. In addition to the
typical operators (+, -, *, etc), dice-specific operators are
provided as well. For example, just as you would write "4 + 3", you
can also write "4 d 3", which rolls 4 dice with three faces each
and adds the result. Some operators only work when certain terms
are to the left of them, like the k operator, which only works on
dice terms.

## Terms
Working with dice expressions is all about working with terms.
There are four types of terms, with specific rules on how to convert
these terms into other types.

### Number
A term can just be a regular rational number. Behind the scenes, a BigDecimal
is used, to make sure there is minimal data loss during computation. However,
at the end of the computation, only four decimal points will be shown, depending
on the rounding mode.

### Dice
A term that represents a set of dice to be rolled. Dice can be created, using a `d`
or a `D` operator on any other terms. There are a number of operators that
work only on dice, such as `k`. Dice are resolved into a number once it is rolled,
which is done as soon as a non-dice operator is used on it, or if it is the only
remaining term in the expression.

As an example, using `4d10` creates four dice with ten faces. At this point,
the dice have not been rolled. We use `k2` on the dice, which indicates that
we should keep the highest two dice and discard the others; however, we
still have not rolled the dice. Once we further add `+1` to the expression,
then the dice are rolled, totaled up, and 1 is added to that result. The term is now
a number, not a dice; we cannot use `4d10+1k2`, as this does not make sense.

### Pool
A pool is a group of one or more terms, which can be any mixture of types.
A pool can be created using a `,` operator on any other terms, including other
pools. Parenthesis around the pool are not required, but easier to read.
The only way to resolve a pool into a number is to reduce it to one element,
by using a `k` for instance.

As an example, using `(4d10,10)` will create a pool of two elements: one is a four ten-sided
dice, and one is the constant number 6. Attempting to use any numerical
operator, like `+`, will fail. Using `k1` will evaluate every element
in the pool and keep the highest one, turning it into a number. However,
using `k2` will just create another pool with the highest two elements.

A pool can be passed as the right parameter of a `d` operator; this allows
non-standard dice, functionally acting as a "randomly pick an item in the pool".

As an example, using `2d(2,4,6,8)` will choose two random numbers from the pool of
2, 4, 6, or 8. 

### Symbol
Coins and Fate Dice are supported in dice expressions. The result of a coin flip
or a fate dice roll is a sequence of symbols; H/T, or +/-/0. Attempting
to use almost any operator on the result of a coin flip or fate die roll will cause an
error. For example, `2dC + 5` will not work, since the result of two coin flips
cannot logically have 5 added to it.

Using `s` or `f` operators will allow you to resolve a coin flip into a number. 
Building on the earlier example, `2dCsH + 5` will work, because
the number of heads will be counted, and 5 can be added to that.

Fate dice cannot be coerced back into a number at this time, but this feature
may be added if it is desired.

## A note on rounding
Many mathematical operators cannot use anything aside from
an integer as an argument, for example, the number of dice to roll. 
At the same time, it is frequent in role-playing games that 
the number of dice to roll is a potentially non-integer expression.
The system typically has a rounding rule that it uses to turn any
non-integer into an integer.

Fracktail accommodates this by allowing you to specify a rounding rule
in the command. The options are explained here:
- Truncate: The decimal portion is chopped off, leaving only the integer portion. e.g. 4.3, 4.5, and 4.9 all becomes 4.
- Ceil: Always increase to the nearest whole number. e.g. 4.3 becomes 5, -1.3 becomes -1.
- Away From Zero: Round moving away from zero. e.g. 4.3 becomes 5, -1.3 becomes -2.
- Toward Zero: Round moving toward zero. e.g. 4.3 becomes 4, -1.3 becomes -1.
- Half Away From Zero: Round to the nearest integer. Halves move away from 0. e.g. 4.3 becomes 4, 4.5 becomes 5, -1.5 becomes -2.
- Half Toward Zero: Round to the nearest integer. Halves move toward 0. e.g. 4.3 becomes 4, 4.5 becomes 4, -1.5 becomes -1
- Half Toward Even: Round to the nearest integer. Halves move toward the even number. e.g. 4.3 becomes 4, 4.5 becomes 4, -1.5 becomes -2

If no rounding mode is specified, the default is Truncate, which is typically
used by many popular campaigns.

## Operator List
All operators will be described here, along with their priority and limitations.
Lower-priority operations will be performed before higher-priority ones.
Operations with the same priority will be evaluated from left-to-right.

### Add/Subtract
- Syntax: `a + b` and `a - b`
- Priority: 10
- Allowed Types: `a` and `b` can be Number or a Dice

Simply add or subtract two terms together.

### Multiply/Divide
- Syntax: `a * b` and `a / b`
- Priority: 8
- Allowed Types: `a` and `b` can be Number or a Dice
- For division, `b` must be non-zero.

Multiply or divide two terms together. 
Division will only compute up to 34 digits, rounding the rest to the
nearest half. 

### Root
- Syntax: `a √ b` computes the a-th root of b.
- Priority: 6
- Allowed Types: `a` and `b` can be Number or a Dice
- `a` must be greater than 0. `b` must be non-negative.
- `a` may be omitted, in which case `2` is the default.
- `a` is coerced into an integer using the rounding rules.

Compute the nth-root of a number. This was included as a joke for Grapha.
There are some special cases:
- If `a` is 1, `b` is returned unchanged
- If `a` is 2, the square root is computed up to 34 digits, rounding to the nearest half.
- If `a` is any other number, there is further loss of digits due to conversion between decimal types on backend.

### Parenthesis/Brackets
- Syntax: `()` or `[]` to group expressions together
- Priority: N/A

Parenthesis or brackets can be used to group operations of lower priority together before
applying a higher-priority operation.
Parenthesis and Brackets do not need to match; Using `(3+5]` is perfectly
valid, despite looking awkward. This is mostly for ease of coding. In the future,
they may be forced to match.

### Dice
- Syntax: `a d b` rolls `a` dice with `b` number of faces
- Priority: 4
- Allowed Types: `a` can be a number or a dice. `b` can be any type.
- `b` must be non-negative, `C`, or `F`.
- `a` may be omitted, in which case `1` is the default.
- `a` and `b` are coerced into integers using the rounding rules.

Create a dice to be rolled. 

`a` may be any integer number. If `a` is 0, no dice are
rolled, and the result is 0. If `a` is negative, `-a` dice are rolled, and the
result is negated.

If `b` is 0, the result is 0. If `b` is a `C`, this dice becomes a coin flip, resulting
in either Heads (`H`) or Tails (`T`). If `b` is a `F`, this dice becomes a fate dice,
result in either `+`, `-`, or `0`. 

Because of order of operations, rolling a `1d4d8` would roll a `1d4` first, and then
roll that many `d8`s for the final result. To invert this, use `1d(4d8)`, which
would roll four `d8`s, then roll 1 dice of that many faces.

### Dot Dice
- Syntax: `a D b` rolls `a` dot dice with `b` number of faces
- Priority: 4
- Allowed Types: `a` can be dice or a number. `b` can be any type.
- `a` may be omitted, in which case `1` is the default.
- `a` and `b` are coerced into integers using the rounding rules.

Dot dice is a shortcut for SCAR-type rolling. This functions identically to a regular
dice roll, but automatically sets up exploding at a 10, and the success
strategy that SCAR uses. It can otherwise be used in the same way as a dice roll.

### Drop
- Syntax: `axb` drops `b` dice from `a`'s roll
- Priority: 4
- Allowed Types: `a` must be a dice. `b` can be a dice or a number.
- `b` is coerced into an integer using the rounding rules.

After rolling the dice, the lowest values are dropped. If `b` is negative, an
error is thrown.

### Keep Highest N/Keep Lowest N
- Syntax: `akb` keeps the highest `b` dice from `a`'s roll. `alb` keeps the lowest `b` dice from `a`'s roll
- Priority: 4
- Allowed Types: `a` must be dice or a pool. `b` can be a dice or a number.
- `b` is coerced into an integer using the rounding rules.

After rolling the dice, the lowest or the highest values are dropped. `k` keeps the
highest, while `l` keeps the lowest.

The difference between Drop and Keep is the number of dice that remain. For example:
- `8x3` would roll eight dice and remove 3. As such, 5 dice remain.
- `8k3` or `8l3` would roll eight dice and remove all but 3.

If a pool is kept, a pool is returned with the highest or lowest elements dropped.
If, after dropping, the pool is just of size one, it becomes a number instead.

e.g., `(1d8, 4)k1`, will become a number equal to either the result of the d8, or 4,
whichever is higher. This allows an effective way to enforce a minimum or maximum value in
a roll.

#### Keep Highest, Keep Lowest
- Syntax: `aK` or `aL`
- Priority: 4
- Allowed Types: `a` must be dice or a pool.

This is syntactic sugar for keeping the highest or lowest dice of the set.
Identical to `ak1` or `al1`.

### Reroll
- Syntax: `arb` rerolls `a` if an element is less than or equal to `b`.
- Priority: 4
- Allowed Types: `a` must be a dice. `b` must be a number or a dice.
- `b` is coerced into an integer using the rounding rules.

Rolls `a` dice, doing one reroll of any dice values that were less than or equal to `b`.
For example, if `4d10` results in `8, 3, 1, 9`, then `4d10r5` would drop the two
values less than or equal to 5, and roll two new dice to replace them. The newly-
rolled dice will remain, even if they are also less than 5.

#### Reroll Infinite
By using a `R` instead of an `r`, the dice will continue to be rerolled as long
as some value is less than or equal to `b`. 

Since this is easy to abuse, the rerolls cap out at 100 retries.

### Explode
- Syntax: `aeb` rolls an `a` for each element after the initial roll greater than or equal to `b`.
- Priority: 4
- Allowed Types: `a` must be a dice. `b` must be a number or a dice.
- `b` is coerced into an integer using the rounding rules.

Rolls `a` dice, and rolls an additional `a` if one of your rolls is greater than
or equal to `b`.
For example, if `4d10` results in `8, 3, 1, 9`, then `4d10e5` would roll two new
`d10`s, and add those values to the total. Even if the new values are still greater
than or equal to 5, no other dice are rolled.

#### Explode Infinite
By using `E` instead of `e`, the dice will continue to explode as long as some
value is greater than or equal to `b`.

Since this is easy to abuse, the explodes cap out at 100.

#### Explode Highest
- Syntax: `a!`
- Priority: 4
- Allowed Types: `a` must be a dice.

Syntactic sugar for infinitely exploding with the highest number as `b`. 

### Success At
- Syntax: `asb` rolls `a` and reports the number of rolls greater than or equal to `b`.
- Priority: 4
- Allowed Types: `a` must be a dice. `b` may be a number or a dice.
- `b` is coerced into an integer using the rounding rules, or may be `H` or `T` for coin flips.

By default, the bot reports the total number after summing all the faces of each die.
By using this command, you switch to a counting mode, where the number of dice passing
a certain test will be reported instead. 

For example, if `4d10` returns `1, 3, 6, 9`, the result is `19`. However,
if `4d10s5` is used, the result is `2`, since two dice are greater than or equal to 5.

### Failure At
- Syntax: `afb` rolls `a` and reports the number of rolls less than or equal to `b`.
- Priority: 4
- Allowed Types: `a` must be a dice. `b` may be a number or a dice.
- `b` is coerced into an integer using the rounding rules, or may be `H` or `T` for coin flips.

By default, the bot reports the total number after summing all the faces of each die.
By using this command, you switch to a counting mode, where the number of dice passing
a certain test will be reported instead. 

Unlike Success At, Failure At is for subtracting instead of adding. Typically, you will
use Success At and Failure At in tandem. Failure can be used on its own, where it will
always report a non-positive number.

For example, if `4d10` returns `1, 3, 6, 9`, the result is `19`. However,
if `4d10s5f1` is used, the result is `1`, since two dice are greater than or equal to 5,
but 1 dice is less than or equal to 1. 

### Ceil
- Syntax: `a^` rounds `a` up to the nearest whole number.
- Priority: 6
- Allowed Types: Dice or Number.

This operator will round the result of a up to the nearest whole number, toward
positive infinity.

### Comma
- Syntax: `a,b` creates a pool containing the contents of `a` and `b`.
- Priority: 20
- Allowed Types: `a` can be any type. `b` can be any type.

If `a` is a number or dice, using a comma will create a pool with the contents
of a and b. If `a` is already a pool, the element will be added to the existing pool.
Note that `b` is always added verbatim to `a`'s pool; if `b` is a pool, the pools are
not concatenated together.

