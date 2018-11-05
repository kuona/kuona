function elapsedFilter() {
  return function (duration) {
    const seconds = Math.floor(duration / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (days > 1) {
      return days + " days";
    } else if (hours > 1) {
      return hours + " hours";
    } else if (minutes > 1) {
      return minutes + " minutes";
    } else {
      return seconds + " seconds";
    }
  }
}

function ageFilter() {
  return function (date) {
    if (!date) return;

    let time;

    if (typeof date === 'string') {
      time = Date.parse(date);
    } else {
      time = new Date(date);
    }

    let timeNow = new Date().getTime(),
      difference = timeNow - time,
      seconds = Math.floor(difference / 1000),
      minutes = Math.floor(seconds / 60),
      hours = Math.floor(minutes / 60),
      days = Math.floor(hours / 24),
      months = Math.floor(days / 30),
      years = Math.floor(days / 365);

    if (years > 0) {
      return years + " years ago";
    } else if (months === 1) {
      return "about a month ago";
    } else if (months > 1) {
      return "about " + months + " months ago";
    } else if (days > 1) {
      return days + " days ago";
    } else if (days === 1) {
      return "1 day ago"
    } else if (hours > 1) {
      return hours + " hours ago";
    } else if (hours === 1) {
      return "an hour ago";
    } else if (minutes > 1) {
      return minutes + " minutes ago";
    } else if (minutes === 1) {
      return "a minute ago";
    } else {
      return seconds + " seconds ago";
    }
  }
}

function registerKuonaAngularFilters(module) {
  module.filter('elapsed', elapsedFilter);
  module.filter('age', ageFilter);
}
