import {commaListToArray} from '../src/js/utils'

describe('String to array processing', () => {
  it('maps an empty string to an empty array', () => {
    expect(commaListToArray("", ",")).toEqual(expect.arrayContaining([]));
  });
  it('maps an single word string to a one element array', () => {
    expect(commaListToArray("foo", ",")).toEqual(expect.arrayContaining(["foo"]));
  });
  it('maps a separated list to an array', () => {
    expect(commaListToArray("foo,bar", ",")).toEqual(expect.arrayContaining(["foo", "bar"]));
  });
  it('maps a separated list to an array trimming white space', () => {
    expect(commaListToArray(" foo , bar ", ",")).toEqual(expect.arrayContaining(["foo", "bar"]));
  });
});
