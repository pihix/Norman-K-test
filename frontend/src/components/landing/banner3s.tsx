import * as React from "react";

import { ArrowRightIcon } from "~components/icons/arrow";

interface Banner3SProps {
  title: string;
  doc: string;
}

const Banner3S: React.FC<React.PropsWithChildren<Banner3SProps>> = ({ title, doc, children }) => (
  <div className="xl:w-1/4 lg:w-1/2 md:w-full px-8 py-6 border-l-2 border-gray-200 border-opacity-60 flex flex-col justify-between">
    <section>
      <h2 className="text-lg sm:text-xl text-gray-900 font-medium title-font mb-2">{title}</h2>
      <p className="leading-relaxed text-base mb-4">{children}</p>
    </section>

    <a href={doc} target="_blank" rel="noreferrer" className="text-red-500 inline-flex items-center">
      Learn More
      <ArrowRightIcon />
    </a>
  </div>
);

export default Banner3S;
